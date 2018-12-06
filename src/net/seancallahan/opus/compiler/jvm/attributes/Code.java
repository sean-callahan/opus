package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.jvm.CodeGenerator;
import net.seancallahan.opus.compiler.jvm.ConstantPool;
import net.seancallahan.opus.lang.Method;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Code extends Attribute
{
    private final Method method;

    private final short maxStack;
    private final short maxLocals;

    private final ByteArrayOutputStream code = new ByteArrayOutputStream();

    private final List<Attribute> attributes = new ArrayList<>();

    public Code(ConstantPool pool, Method method) throws CompilerException
    {
        super(pool, "Code");

        this.method = method;

        CodeGenerator gen = new CodeGenerator(this);
        this.maxStack = gen.getMaxStack();
        this.maxLocals = gen.getMaxLocalVars();
    }

    public Method getMethod()
    {
        return method;
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