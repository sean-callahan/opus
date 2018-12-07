package net.seancallahan.opus.compiler.jvm;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.Operator;
import net.seancallahan.opus.compiler.jvm.attributes.Code;
import net.seancallahan.opus.compiler.jvm.attributes.LineNumberTable;
import net.seancallahan.opus.compiler.jvm.attributes.LocalVariableTable;
import net.seancallahan.opus.compiler.parser.Expression;
import net.seancallahan.opus.compiler.parser.Statement;
import net.seancallahan.opus.lang.Method;
import net.seancallahan.opus.lang.Type;
import net.seancallahan.opus.lang.Variable;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator
{
    // TODO: move this to an Attribute subclass

    private short maxStack;
    private short stackSize;
    private short maxLocalVars;

    private final ConstantPool pool;
    private final ByteArrayOutputStream code;

    private final LocalVariableTable localVariableTable;

    private Map<Variable, Byte> variables = new HashMap<>();
    private byte nextVariable = 1;

    public CodeGenerator(Code attribute) throws CompilerException
    {
        Method method = attribute.getMethod();

        this.pool = attribute.getPool();

        this.localVariableTable = new LocalVariableTable(pool);
        LineNumberTable lineNumberTable = new LineNumberTable(pool);

        this.code = attribute.getCode();

        if (method == null)
        {
            return;
        }

        attribute.getAttributes().add(localVariableTable);
        attribute.getAttributes().add(lineNumberTable);

        for (Statement stmt : method.getBody().getStatements())
        {
            if (stmt == null)
            {
                continue;
            }

            if (stmt instanceof Statement.VariableDeclaration)
            {
                variableDeclaration((Statement.VariableDeclaration)stmt);
            }
            else if (stmt instanceof Statement.Assignment)
            {
                assignment((Statement.Assignment)stmt);
            }
            else if (stmt instanceof Statement.Return)
            {
                returnStatement((Statement.Return)stmt);
            }

            if (stmt.getStartPosition() != null)
            {
                short line = (short)stmt.getStartPosition().getLine();
                if (!lineNumberTable.contains(line))
                {
                    lineNumberTable.add(line, (short)code.size());
                }
            }
        }

        List<Statement> stmts = method.getBody().getStatements();

        Statement last = stmts.size() > 1 ? stmts.get(stmts.size()-1) : null;

        if (last != null && !(last instanceof Statement.Return))
        {
            add(Instruction._return);
        }
    }

    public short getMaxStack()
    {
        return maxStack;
    }

    public short getMaxLocalVars()
    {
        return maxLocalVars;
    }

    private void add(Instruction instruction, byte... args)
    {
        code.write(instruction.getOpcode());
        for (byte arg : args)
        {
            code.write(arg);
        }
    }

    private void expr(Expression expr)
    {
        // NOTE: the expression _should_ have matching types at this point.
        // TODO: convert non-matching types

        if (expr instanceof Expression.Binary)
        {
            binary((Expression.Binary) expr);
        }
        else if (expr instanceof Expression.Unary)
        {
            unary((Expression.Unary) expr);
        }
        else if (expr instanceof Expression.Literal)
        {
            add(Instruction.bipush, Byte.valueOf(((Expression.Literal) expr).getToken().getValue()));
        }
    }

    private void binary(Expression.Binary expr)
    {
        expr(expr.getLeft());
        expr(expr.getRight());
        add(getOpInstruction(expr.getType(), expr.getOperator()));
    }

    private void unary(Expression.Unary expr)
    {
        expr(expr.getRight());
        add(getOpInstruction(expr.getType(), expr.getOperator()));
    }

    private void variableDeclaration(Statement.VariableDeclaration declaration)
    {
        Type type = declaration.getVariable().getType();
        if (!type.isPrimitive())
        {
            return;
        }

        Instruction[] store1to3 = null;
        Instruction store = null;

        // push default value
        switch (type.getName())
        {
            case "s8":
            case "u8":
            case "s16":
            case "u16":
            case "s32":
            case "u32":
                add(Instruction.iconst_0);
                store1to3 = new Instruction[] { Instruction.istore_1, Instruction.istore_2, Instruction.istore_3 };
                store = Instruction.istore;
                break;
            case "s64":
            case "u64":
                add(Instruction.lconst_0);
                store1to3 = new Instruction[] { Instruction.lstore_1, Instruction.lstore_2, Instruction.lstore_3 };
                store = Instruction.lstore;
                break;
            case "f32":
                add(Instruction.fconst_0);
                store1to3 = new Instruction[] { Instruction.fstore_1, Instruction.fstore_2, Instruction.fstore_3 };
                store = Instruction.fstore;
                break;
            case "f64":
                add(Instruction.dconst_0);
                store1to3 = new Instruction[] { Instruction.dstore_1, Instruction.dstore_2, Instruction.dstore_3 };
                store = Instruction.dstore;
                break;
            default:
                throw new UnsupportedOperationException("type not supported");
        }

        short length;

        if (this.nextVariable > 3)
        {
            this.add(store, this.nextVariable);
            length = 3;
        }
        else
        {
            this.add(store1to3[this.nextVariable - 1]);
            length = 2;
        }

        localVariableTable.add(declaration.getVariable(), (short)code.size(), length, nextVariable);

        variables.put(declaration.getVariable(), this.nextVariable);

        this.nextVariable++;
        maxLocalVars++;
    }

    private void assignment(Statement.Assignment assignment) throws CompilerException
    {
        Variable var = null;
        byte index = 0;
        for (Variable variable : variables.keySet())
        {
            if (assignment.getName().getValue().equals(variable.getName().getValue()))
            {
                var = variable;
                index = variables.get(variable);
                break;
            }
        }
        if (var == null)
        {
            throw new CompilerException("cannot assign to undeclared variable");
        }

        Expression expr = assignment.getExpression();

        if (!(expr instanceof Expression.Literal))
        {
            this.expr(expr);
            return;
        }

        Expression.Literal literal = (Expression.Literal) expr;

        long value = Long.parseLong(literal.getToken().getValue());
        // NOTE: ONLY SUPPORTS 1 << 63 due to max size of long
        // TODO: floats

        if (value < 1 << Byte.SIZE)
        {
            // TODO: add iconst_n instructions
            pushSize(1);
            add(Instruction.bipush, (byte) value);
            add(Instruction.istore, index);
            add(Instruction.pop);
            popSize(1);
            return;
        }

        if (value < (1 << Short.SIZE))
        {
            pushSize(2);
            add(Instruction.sipush, (byte) (value & 0xff), (byte) ((value >> 8) & 0xff));
            add(Instruction.istore, index);
            add(Instruction.pop);
            popSize(2);
            return;
        }

        if (value < 1 << Integer.SIZE)
        {
            short poolIndex = pool.add(new Constant<>(Constant.Kind.INTEGER, (int) value));
            pushSize(2);
            add(Instruction.ldc_w, (byte) (poolIndex & 0xff), (byte) ((poolIndex >> 8) & 0xff));
            add(Instruction.istore, index);
            add(Instruction.pop);
            popSize(2);
            return;
        }

        if (value < 1 << Long.SIZE)
        {
            short poolIndex = pool.add(new Constant<>(Constant.Kind.LONG, value));
            pushSize(2);
            add(Instruction.ldc2_w, (byte) (poolIndex & 0xff), (byte) ((poolIndex >> 8) & 0xff));
            add(Instruction.lstore, index);
            add(Instruction.pop2);
            popSize(2);
            return;
        }

        throw new UnsupportedOperationException("value too large");
    }

    private void pushSize(int size)
    {
        stackSize += size;
        if (stackSize > maxStack)
        {
            maxStack = stackSize;
        }
    }

    private void popSize(int size)
    {
        assert stackSize >= size;
        stackSize -= size;
    }

    private void returnStatement(Statement.Return stmt)
    {
        expr(stmt.getExpression());
    }

    private static final Map<String, Map<Operator, Instruction>> opTable = new HashMap<>();

    static
    {
        final Map<Operator, Instruction> i32 = new HashMap<>();
        i32.put(Operator.ADD, Instruction.iadd);
        i32.put(Operator.SUBTRACT, Instruction.isub);
        i32.put(Operator.MULTIPLY, Instruction.imul);
        i32.put(Operator.DIVIDE, Instruction.idiv);
        i32.put(Operator.MOD, Instruction.irem);
        i32.put(Operator.LSHIFT, Instruction.ishl);
        i32.put(Operator.RSHIFT, Instruction.ishr);
        opTable.put("s32", i32);
        opTable.put("u32", i32);

        final Map<Operator, Instruction> i64 = new HashMap<>();
        i64.put(Operator.ADD, Instruction.ladd);
        i64.put(Operator.SUBTRACT, Instruction.lsub);
        i64.put(Operator.MULTIPLY, Instruction.lmul);
        i64.put(Operator.DIVIDE, Instruction.ldiv);
        i64.put(Operator.MOD, Instruction.lrem);
        i64.put(Operator.LSHIFT, Instruction.lshl);
        i64.put(Operator.RSHIFT, Instruction.lshr);
        opTable.put("s64", i64);
        opTable.put("u64", i64);

        final Map<Operator, Instruction> f32 = new HashMap<>();
        f32.put(Operator.ADD, Instruction.fadd);
        f32.put(Operator.SUBTRACT, Instruction.fsub);
        f32.put(Operator.MULTIPLY, Instruction.fmul);
        f32.put(Operator.DIVIDE, Instruction.fdiv);
        f32.put(Operator.MOD, Instruction.frem);
        opTable.put("f32", f32);

        final Map<Operator, Instruction> f64 = new HashMap<>();
        f64.put(Operator.ADD, Instruction.dadd);
        f64.put(Operator.SUBTRACT, Instruction.dsub);
        f64.put(Operator.MULTIPLY, Instruction.dmul);
        f64.put(Operator.DIVIDE, Instruction.ddiv);
        f64.put(Operator.MOD, Instruction.drem);
        opTable.put("f64", f64);
    }

    private static Instruction getOpInstruction(Type type, Operator operator)
    {
        if (!type.isPrimitive())
        {
            throw new UnsupportedOperationException("only operations on primitive types are supported");
        }

        Map<Operator, Instruction> instructions = opTable.get(type.getName());
        if (!instructions.containsKey(operator))
        {
            throw new UnsupportedOperationException("operator not supported for this type");
        }

        return instructions.get(operator);
    }
}
