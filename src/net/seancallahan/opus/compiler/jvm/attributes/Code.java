package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.Function;
import net.seancallahan.opus.compiler.jvm.ClassFile;
import net.seancallahan.opus.compiler.jvm.CodeGenerator;

import java.nio.ByteBuffer;

public class Code extends AttributeParent
{
    private static final int MAX_CODE_SIZE = 1 << 10;

    private final Function function;

    private final short maxStack;
    private final short maxLocals;

    private final ByteBuffer code = ByteBuffer.allocateDirect(MAX_CODE_SIZE);

    public Code(ClassFile file, ByteBuffer buffer, Function function) throws CompilerException
    {
        super(file.getConstantPool(), buffer, "Code");

        this.function = function;

        CodeGenerator gen = new CodeGenerator(file, getAttributeBuffer(), this);

        this.maxStack = gen.getMaxStack();
        this.maxLocals = gen.getMaxLocalVars();
    }

    public Function getFunction()
    {
        return function;
    }

    public ByteBuffer getCode()
    {
        return code;
    }

    @Override
    public void write(ByteBuffer out)
    {
        body.putShort(maxStack);
        body.putShort(maxLocals);

        body.putInt(code.position());
        body.put(code);

        body.putShort((short)0); // exception_table_length

        body.putShort((short)getAttributes().size());
        for (Attribute attribute : getAttributes())
        {
            attribute.write(body);
        }

        super.write(out);
    }
}