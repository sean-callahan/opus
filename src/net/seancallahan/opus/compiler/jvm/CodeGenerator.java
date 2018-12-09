package net.seancallahan.opus.compiler.jvm;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.Function;
import net.seancallahan.opus.compiler.Operator;
import net.seancallahan.opus.compiler.jvm.attributes.Code;
import net.seancallahan.opus.compiler.jvm.attributes.LineNumberTable;
import net.seancallahan.opus.compiler.jvm.attributes.LocalVariableTable;
import net.seancallahan.opus.compiler.parser.Body;
import net.seancallahan.opus.compiler.parser.Expression;
import net.seancallahan.opus.compiler.parser.Statement;
import net.seancallahan.opus.lang.Type;
import net.seancallahan.opus.lang.Variable;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator
{
    // TODO: move this to an Attribute subclass

    private final Function function;

    private short maxStack;
    private short stackSize;
    private short maxLocalVars;

    private final ConstantPool pool;
    private final ByteArrayOutputStream code;

    private final LocalVariableTable localVariableTable;
    private final LineNumberTable lineNumberTable;

    private Map<String, Byte> variables = new HashMap<>();
    private byte nextVariable = 0;

    public CodeGenerator(Code attribute) throws CompilerException
    {
        this.function = attribute.getFunction();
        this.pool = attribute.getPool();
        this.localVariableTable = new LocalVariableTable(pool);
        this.lineNumberTable = new LineNumberTable(pool);
        this.code = attribute.getCode();

        if (function == null)
        {
            return;
        }

        for (Variable param : function.getParameters())
        {
            maxLocalVars += param.getType().getStackSize();
            variables.put(param.getName().getValue(), this.nextVariable);
            nextVariable++;
        }

        attribute.getAttributes().add(localVariableTable);
        attribute.getAttributes().add(lineNumberTable);

        generateBody(code, function.getBody());

        List<Statement> stmts = function.getBody().getStatements();

        Statement last = stmts.size() > 1 ? stmts.get(stmts.size()-1) : null;

        if (last != null && !(last instanceof Statement.Return))
        {
            add(code, Instruction._return);
        }
    }

    private void generateBody(ByteArrayOutputStream out, Body body) throws CompilerException
    {
        for (Statement stmt : body.getStatements())
        {
            if (stmt == null)
            {
                continue;
            }

            if (stmt instanceof Statement.VariableDeclaration)
            {
                variableDeclaration(out, (Statement.VariableDeclaration)stmt);
            }
            else if (stmt instanceof Statement.Assignment)
            {
                assignment(out, (Statement.Assignment)stmt);
            }
            else if (stmt instanceof Statement.Return)
            {
                returnStatement(out, (Statement.Return)stmt);
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
    }

    public short getMaxStack()
    {
        return maxStack;
    }

    public short getMaxLocalVars()
    {
        return maxLocalVars;
    }

    private static void add(ByteArrayOutputStream out, Instruction instruction, byte... args)
    {
        out.write(instruction.getOpcode());
        out.write(args, 0, args.length);
    }

    private void expr(ByteArrayOutputStream out, Expression expr)
    {
        // NOTE: the expression _should_ have matching types at this point.
        // TODO: convert non-matching types

        if (expr instanceof Expression.Binary)
        {
            binary(out, (Expression.Binary) expr);
        }
        else if (expr instanceof Expression.Unary)
        {
            unary(out, (Expression.Unary) expr);
        }
        else if (expr instanceof Expression.Group)
        {
            expr(out, ((Expression.Group) expr).getInner());
        }
        else if (expr instanceof Expression.Literal)
        {
            literal(out, (Expression.Literal) expr);
        }
    }

    private void binary(ByteArrayOutputStream out, Expression.Binary expr)
    {
        expr(out, expr.getLeft());
        expr(out, expr.getRight());
        add(out, getOpInstruction(expr.getLeft().getType(), expr.getOperator()));
    }

    private void unary(ByteArrayOutputStream out, Expression.Unary expr)
    {
        expr(out, expr.getRight());
        add(out, getOpInstruction(expr.getType(), expr.getOperator()));
    }

    private void literal(ByteArrayOutputStream out, Expression.Literal expr)
    {
        String text = expr.getToken().getValue();

        if (variables.containsKey(text))
        {
            loadNumber(out, expr.getType(), variables.get(text));
            return;
        }

        pushNumber(out, expr.getType(), expr.getToken().getValue());
    }

    private void variableDeclaration(ByteArrayOutputStream out, Statement.VariableDeclaration declaration)
    {
        Type type = declaration.getVariable().getType();
        if (!type.isPrimitive())
        {
            return;
        }

        if (declaration.getExpression() == null)
        {
            pushNumber(out, type, "0");
        }
        else
        {
            expr(out, declaration.getExpression());
        }

        int length = storeLast(out, type, nextVariable);

        localVariableTable.add(declaration.getVariable(), (short)code.size(), (short)3, nextVariable);

        variables.put(declaration.getVariable().getName().getValue(), this.nextVariable);

        nextVariable += length;
        maxLocalVars += length;
    }

    private void assignment(ByteArrayOutputStream out, Statement.Assignment assignment) throws CompilerException
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
        this.expr(out, expr);

        storeLast(out, expr.getType(), index);
    }

    private static int storeLast(ByteArrayOutputStream out, Type type, byte index)
    {
        String name = type.getName();
        if (name.equals("u64") || name.equals("s64") || name.equals("f64"))
        {
            if (name.startsWith("f"))
            {
                // double
                switch (index)
                {
                    case 0: add(out, Instruction.dstore_0); break;
                    case 1: add(out, Instruction.dstore_1); break;
                    case 2: add(out, Instruction.dstore_2); break;
                    case 3: add(out, Instruction.dstore_3); break;
                    default: add(out, Instruction.dstore, index);
                }
            }
            else
            {
                // long
                switch (index)
                {
                    case 0: add(out, Instruction.lstore_0); break;
                    case 1: add(out, Instruction.lstore_1); break;
                    case 2: add(out, Instruction.lstore_2); break;
                    case 3: add(out, Instruction.lstore_3); break;
                    default: add(out, Instruction.lstore, index);
                }
            }
            return 2;
        }

        if (name.startsWith("f"))
        {
            // float
            switch (index)
            {
                case 0: add(out, Instruction.fstore_0); break;
                case 1: add(out, Instruction.fstore_1); break;
                case 2: add(out, Instruction.fstore_2); break;
                case 3: add(out, Instruction.fstore_3); break;
                default: add(out, Instruction.fstore, index);
            }
        }
        else
        {
            // int
            switch (index)
            {
                case 0: add(out, Instruction.istore_0); break;
                case 1: add(out, Instruction.istore_1); break;
                case 2: add(out, Instruction.istore_2); break;
                case 3: add(out, Instruction.istore_3); break;
                default: add(out, Instruction.istore, index);
            }
        }
        return 1;
    }

    private int pushNumber(ByteArrayOutputStream out, Type type, String value)
    {
        if (type.getName().startsWith("f"))
        {
            return pushFloat(out, type, Double.parseDouble(value));
        }

        return pushInteger(out, type, Long.parseLong(value));
    }

    private int pushInteger(ByteArrayOutputStream out, Type type, long value)
    {
        String name = type.getName();

        if (value <= 1 && (name.equals("s64") || name.equals("u64")))
        {
            if (value == 0)
            {
                add(out, Instruction.lconst_0);
                return 0;
            }
            add(out, Instruction.lconst_1);
            return 0;
        }

        if (value <= 5 && !(name.equals("s64") || name.equals("u64")))
        {
            switch ((byte)value)
            {
                case 0: add(out, Instruction.iconst_0); return 0;
                case 1: add(out, Instruction.iconst_1); return 0;
                case 2: add(out, Instruction.iconst_2); return 0;
                case 3: add(out, Instruction.iconst_3); return 0;
                case 4: add(out, Instruction.iconst_4); return 0;
                case 5: add(out, Instruction.iconst_5); return 0;
            }
            return 0;
        }

        short stackSize;

        switch (type.getName())
        {
            case "u8":
            case "s8":
                stackSize = 1;
                add(out, Instruction.bipush, (byte)value);
                break;
            case "u16":
            case "s16":
                stackSize = 2;
                add(out, Instruction.sipush, (byte)(value & 0xff), (byte)((value >> 8) & 0xff));
                break;
            case "u32":
            case "s32":
                stackSize = 2;
                short index = pool.add(new Constant<>(Constant.Kind.INTEGER, (int)value));
                add(out, Instruction.ldc_w, (byte)(index & 0xff), (byte)((index >> 8) & 0xff));
                break;
            case "u64":
            case "s64":
                stackSize = 2;
                index = pool.add(new Constant<>(Constant.Kind.LONG, value));
                add(out, Instruction.ldc2_w, (byte)(index & 0xff), (byte)((index >> 8) & 0xff));
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

    private int pushFloat(ByteArrayOutputStream out, Type type, double value)
    {
        switch (type.getName())
        {
            case "f32":
                short index = pool.add(new Constant<>(Constant.Kind.FLOAT, (float)value));
                add(out, Instruction.ldc_w, (byte)(index & 0xff), (byte)((index >> 8) & 0xff));
                break;
            case "f64":
                index = pool.add(new Constant<>(Constant.Kind.DOUBLE, value));
                add(out, Instruction.ldc2_w, (byte)(index & 0xff), (byte)((index >> 8) & 0xff));
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

    public void loadNumber(ByteArrayOutputStream out, Type type, byte index)
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
                    case 0: add(out, Instruction.dload_0); break;
                    case 1: add(out, Instruction.dload_1); break;
                    case 2: add(out, Instruction.dload_2); break;
                    case 3: add(out, Instruction.dload_3); break;
                    default: add(out, Instruction.dload, index);
                }
            }
            else
            {
                // long
                switch (index)
                {
                    case 0: add(out, Instruction.lload_0); break;
                    case 1: add(out, Instruction.lload_1); break;
                    case 2: add(out, Instruction.lload_2); break;
                    case 3: add(out, Instruction.lload_3); break;
                    default: add(out, Instruction.lload, index);
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
                    case 0: add(out, Instruction.fload_0); break;
                    case 1: add(out, Instruction.fload_1); break;
                    case 2: add(out, Instruction.fload_2); break;
                    case 3: add(out, Instruction.fload_3); break;
                    default: add(out, Instruction.fload, index); break;
                }
            }
            else
            {
                // int
                switch (index)
                {
                    case 0: add(out, Instruction.iload_0); break;
                    case 1: add(out, Instruction.iload_1); break;
                    case 2: add(out, Instruction.iload_2); break;
                    case 3: add(out, Instruction.iload_3); break;
                    default: add(out, Instruction.iload, index); break;
                }
            }

            length = 1;
        }

        if (this.stackSize < length)
        {
            this.stackSize = length;
        }
    }

    private void returnStatement(ByteArrayOutputStream out, Statement.Return stmt)
    {
        Expression expr = stmt.getExpression();
        if (expr == null)
        {
            add(out, Instruction._return);
            return;
        }

        this.expr(out, expr);

        String name = expr.getType().getName();

        if (name.equals("u64") || name.equals("s64") || name.equals("f64"))
        {
            if (name.startsWith("f"))
            {
                // double
                add(out, Instruction.dreturn);
                return;
            }

            // long
            add(out, Instruction.lreturn);
            return;
        }

        if (name.startsWith("f"))
        {
            // floats
            add(out, Instruction.freturn);
            return;
        }

        // int
        add(out, Instruction.ireturn);
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
