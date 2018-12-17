package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.jvm.Constant;
import net.seancallahan.opus.compiler.jvm.ConstantPool;

import java.nio.ByteBuffer;

public abstract class Attribute
{
    protected final ConstantPool pool;
    private final short nameIndex;

    protected final ByteBuffer body;

    protected Attribute(ConstantPool pool, ByteBuffer buffer, String name)
    {
        this.pool = pool;
        this.body = buffer;
        this.nameIndex = pool.add(new Constant.UTF8(pool, name));
    }

    public void write(ByteBuffer out)
    {
        out.putShort(nameIndex);
        out.putInt((short)body.position());
        out.put(body);
        body.clear();
    }

    public ByteBuffer getBody()
    {
        return body;
    }

    public ConstantPool getPool()
    {
        return pool;
    }

}
