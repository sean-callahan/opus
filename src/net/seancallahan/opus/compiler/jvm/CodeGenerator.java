package net.seancallahan.opus.compiler.jvm;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.parser.Statement;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Method;
import net.seancallahan.opus.lang.Type;
import net.seancallahan.opus.lang.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator
{
    private static final int INT_SIZE = 4;
    private static final int LONG_SIZE = 8;
    private static final int FLOAT_SIZE = 4;
    private static final int DOUBLE_SIZE = 8;

    private int stackSize;

    private final List<Byte> code = new ArrayList<>();

    private Map<Variable, Byte> variables = new HashMap<>();
    private byte nextVariable = 1;

    public CodeGenerator(Method method) throws CompilerException
    {
        for (Statement stmt : method.getBody())
        {
            if (stmt instanceof Statement.VariableDeclaration)
            {
                variableDeclaration(this, (Statement.VariableDeclaration)stmt);
            }
        }
    }

    public int getStackSize()
    {
        return stackSize;
    }

    public byte[] getCode()
    {
        byte[] code = new byte[this.code.size()];
        for (int i = 0; i < code.length; i ++)
        {
            code[i] = this.code.get(i);
        }
        return code;
    }

    private void add(Instruction instruction, byte... args)
    {
        code.add(instruction.getOpcode());
        for (byte arg : args)
        {
            code.add(arg);
        }
    }

    private static void expr()
    {

    }

    private static void statement(Declaration declaration)
    {

    }

    private static void variableDeclaration(CodeGenerator gen, Statement.VariableDeclaration declaration)
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
                gen.add(Instruction.iconst_0);
                store1to3 = new Instruction[] { Instruction.istore_1, Instruction.istore_2, Instruction.istore_3 };
                store = Instruction.istore;
                gen.stackSize += INT_SIZE;
                break;
            case "s64":
            case "u64":
                gen.add(Instruction.lconst_0);
                store1to3 = new Instruction[] { Instruction.lstore_1, Instruction.lstore_2, Instruction.lstore_3 };
                store = Instruction.lstore;
                gen.stackSize += LONG_SIZE;
                break;
            case "f32":
                gen.add(Instruction.fconst_0);
                store1to3 = new Instruction[] { Instruction.fstore_1, Instruction.fstore_2, Instruction.fstore_3 };
                store = Instruction.fstore;
                gen.stackSize += FLOAT_SIZE;
                break;
            case "f64":
                gen.add(Instruction.dconst_0);
                store1to3 = new Instruction[] { Instruction.dstore_1, Instruction.dstore_2, Instruction.dstore_3 };
                store = Instruction.dstore;
                gen.stackSize += DOUBLE_SIZE;
                break;
            default:
                throw new UnsupportedOperationException("type not supported");
        }

        if (gen.nextVariable > 3)
        {
            gen.add(store, gen.nextVariable);
        }
        else
        {
            gen.add(store1to3[gen.nextVariable-1]);
        }

        gen.nextVariable++;
    }

    private static void assignment()
    {

    }
}
