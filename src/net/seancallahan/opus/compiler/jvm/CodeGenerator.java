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

    private Map<String, Byte> variables = new HashMap<>();
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
        else if (expr instanceof Expression.Group)
        {
            expr(((Expression.Group) expr).getInner());
        }
        else if (expr instanceof Expression.Literal)
        {
            literal((Expression.Literal) expr);
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

    private void literal(Expression.Literal expr)
    {
        String text = expr.getToken().getValue();

        if (variables.containsKey(text))
        {
            loadNumber(expr.getType(), variables.get(text));
            return;
        }

        pushNumber(expr.getType(), expr.getToken().getValue());
    }

    private void variableDeclaration(Statement.VariableDeclaration declaration)
    {
        Type type = declaration.getVariable().getType();
        if (!type.isPrimitive())
        {
            return;
        }

        if (declaration.getExpression() == null)
        {
            pushNumber(type, "0");
        }
        else
        {
            expr(declaration.getExpression());
        }

        int length = storeLast(type, nextVariable);

        localVariableTable.add(declaration.getVariable(), (short)code.size(), (short)3, nextVariable);

        variables.put(declaration.getVariable().getName().getValue(), this.nextVariable);

        nextVariable += length;
        maxLocalVars += length;
    }

    private void assignment(Statement.Assignment assignment) throws CompilerException
    {
        String var = null;
        byte index = 0;
        for (String variable : variables.keySet())
        {
            if (assignment.getName().getValue().equals(variable))
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
        this.expr(expr);

        storeLast(expr.getType(), index);
    }

    private int storeLast(Type type, byte index)
    {
        String name = type.getName();
        if (name.equals("u64") || name.equals("s64") || name.equals("f64"))
        {
            if (name.startsWith("f"))
            {
                // double
                switch (index)
                {
                    case 0: add(Instruction.dstore_0); break;
                    case 1: add(Instruction.dstore_1); break;
                    case 2: add(Instruction.dstore_2); break;
                    case 3: add(Instruction.dstore_3); break;
                    default: add(Instruction.dstore, index);
                }
            }
            else
            {
                // long
                switch (index)
                {
                    case 0: add(Instruction.lstore_0); break;
                    case 1: add(Instruction.lstore_1); break;
                    case 2: add(Instruction.lstore_2); break;
                    case 3: add(Instruction.lstore_3); break;
                    default: add(Instruction.lstore, index);
                }
            }
            return 2;
        }

        if (name.startsWith("f"))
        {
            // float
            switch (index)
            {
                case 0: add(Instruction.fstore_0); break;
                case 1: add(Instruction.fstore_1); break;
                case 2: add(Instruction.fstore_2); break;
                case 3: add(Instruction.fstore_3); break;
                default: add(Instruction.fstore, index);
            }
        }
        else
        {
            // int
            switch (index)
            {
                case 0: add(Instruction.istore_0); break;
                case 1: add(Instruction.istore_1); break;
                case 2: add(Instruction.istore_2); break;
                case 3: add(Instruction.istore_3); break;
                default: add(Instruction.istore, index);
            }
        }
        return 1;
    }

    private int pushNumber(Type type, String value)
    {
        if (type.getName().startsWith("f"))
        {
            return pushFloat(type, Double.parseDouble(value));
        }

        return pushInteger(type, Long.parseLong(value));
    }

    private int pushInteger(Type type, long value)
    {
        String name = type.getName();

        if (value <= 1 && (name.equals("s64") || name.equals("u64")))
        {
            if (value == 0)
            {
                add(Instruction.lconst_0);
                return 0;
            }
            add(Instruction.lconst_1);
            return 0;
        }

        if (value <= 5 && !(name.equals("s64") || name.equals("u64")))
        {
            switch ((byte)value)
            {
                case 0: add(Instruction.iconst_0); return 0;
                case 1: add(Instruction.iconst_1); return 0;
                case 2: add(Instruction.iconst_2); return 0;
                case 3: add(Instruction.iconst_3); return 0;
                case 4: add(Instruction.iconst_4); return 0;
                case 5: add(Instruction.iconst_5); return 0;
            }
            return 0;
        }

        short stackSize;

        switch (type.getName())
        {
            case "u8":
            case "s8":
                stackSize = 1;
                add(Instruction.bipush, (byte)value);
                break;
            case "u16":
            case "s16":
                stackSize = 2;
                add(Instruction.sipush, (byte)(value & 0xff), (byte)((value >> 8) & 0xff));
                break;
            case "u32":
            case "s32":
                stackSize = 2;
                short index = pool.add(new Constant<>(Constant.Kind.INTEGER, (int)value));
                add(Instruction.ldc_w, (byte)(index & 0xff), (byte)((index >> 8) & 0xff));
                break;
            case "u64":
            case "s64":
                stackSize = 2;
                index = pool.add(new Constant<>(Constant.Kind.LONG, value));
                add(Instruction.ldc2_w, (byte)(index & 0xff), (byte)((index >> 8) & 0xff));
                break;
            default:
                throw new UnsupportedOperationException("invalid integer type");
        }

        if (stackSize > this.stackSize)
        {
            this.stackSize = stackSize;
        }

        return stackSize;
    }

    private int pushFloat(Type type, double value)
    {
        switch (type.getName())
        {
            case "f32":
                short index = pool.add(new Constant<>(Constant.Kind.FLOAT, (float)value));
                add(Instruction.ldc_w, (byte)(index & 0xff), (byte)((index >> 8) & 0xff));
                break;
            case "f64":
                index = pool.add(new Constant<>(Constant.Kind.DOUBLE, value));
                add(Instruction.ldc2_w, (byte)(index & 0xff), (byte)((index >> 8) & 0xff));
                break;
            default:
                throw new UnsupportedOperationException("invalid floating point type");
        }

        if (this.stackSize < 2)
        {
            this.stackSize = 2;
        }

        return 2;
    }

    public void loadNumber(Type type, byte index)
    {
        String name = type.getName();

        short length;

        if (name.equals("u64") || name.equals("s64") || name.equals("f64"))
        {
            if (name.startsWith("f"))
            {
                // double
                switch (index)
                {
                    case 0: add(Instruction.dload_0); break;
                    case 1: add(Instruction.dload_1); break;
                    case 2: add(Instruction.dload_2); break;
                    case 3: add(Instruction.dload_3); break;
                    default: add(Instruction.dload, index);
                }
            }
            else
            {
                // long
                switch (index)
                {
                    case 0: add(Instruction.lload_0); break;
                    case 1: add(Instruction.lload_1); break;
                    case 2: add(Instruction.lload_2); break;
                    case 3: add(Instruction.lload_3); break;
                    default: add(Instruction.lload, index);
                }
            }

            length = 2;
        }
        else
        {
            if (name.startsWith("f"))
            {
                // float
                switch (index)
                {
                    case 0: add(Instruction.fload_0); break;
                    case 1: add(Instruction.fload_1); break;
                    case 2: add(Instruction.fload_2); break;
                    case 3: add(Instruction.fload_3); break;
                    default: add(Instruction.fload, index); break;
                }
            }
            else
            {
                // int
                switch (index)
                {
                    case 0: add(Instruction.iload_0); break;
                    case 1: add(Instruction.iload_1); break;
                    case 2: add(Instruction.iload_2); break;
                    case 3: add(Instruction.iload_3); break;
                    default: add(Instruction.iload, index); break;
                }
            }

            length = 1;
        }

        if (this.stackSize < length)
        {
            this.stackSize = length;
        }
    }

    private void returnStatement(Statement.Return stmt)
    {
        Expression expr = stmt.getExpression();
        if (expr == null)
        {
            add(Instruction._return);
            return;
        }

        this.expr(expr);

        String name = expr.getType().getName();

        if (name.equals("u64") || name.equals("s64") || name.equals("f64"))
        {
            if (name.startsWith("f"))
            {
                // double
                add(Instruction.dreturn);
                return;
            }

            // long
            add(Instruction.lreturn);
            return;
        }

        if (name.startsWith("f"))
        {
            // floats
            add(Instruction.freturn);
            return;
        }

        // int
        add(Instruction.ireturn);
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
