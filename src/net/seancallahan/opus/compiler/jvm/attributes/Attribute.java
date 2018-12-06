package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.jvm.Constant;
import net.seancallahan.opus.compiler.jvm.ConstantPool;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Attribute
{
    protected final ConstantPool pool;
    private final short nameIndex;

    private final ByteArrayOutputStream internalBuffer = new ByteArrayOutputStream();
    protected final DataOutputStream buffer = new DataOutputStream(internalBuffer);

    protected Attribute(ConstantPool pool, String name)
    {
        this.pool = pool;
        this.nameIndex = pool.add(new Constant<>(Constant.Kind.UTF8, name));
    }

    public void write(DataOutputStream out) throws IOException
    {
        out.writeShort(nameIndex);
        out.writeInt(buffer.size());
        internalBuffer.writeTo(out);
    }

    public ConstantPool getPool()
    {
        return pool;
    }

}
