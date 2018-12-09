package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.Function;
import net.seancallahan.opus.compiler.jvm.ClassFile;
import net.seancallahan.opus.compiler.jvm.CodeGenerator;
import net.seancallahan.opus.compiler.jvm.ConstantPool;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Code extends Attribute
{
    private final Function function;

    private final short maxStack;
    private final short maxLocals;

    private final ByteArrayOutputStream code = new ByteArrayOutputStream();

    private final List<Attribute> attributes = new ArrayList<>();

    public Code(ClassFile file, Function function) throws CompilerException
    {
        super(file.getConstantPool(), "Code");

        this.function = function;

        CodeGenerator gen = new CodeGenerator(file, this);

        this.maxStack = gen.getMaxStack();
        this.maxLocals = gen.getMaxLocalVars();
    }

    public Function getFunction()
    {
        return function;
    }

    public List<Attribute> getAttributes()
    {
        return attributes;
    }

    public ByteArrayOutputStream getCode()
    {
        return code;
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        buffer.writeShort(maxStack);
        buffer.writeShort(maxLocals);

        buffer.writeInt(code.size());
        code.writeTo(buffer);

        buffer.writeShort(0); // exception_table_length

        buffer.writeShort(attributes.size());
        for (Attribute attribute : attributes)
        {
            attribute.write(buffer);
        }

        super.write(out);
    }
}