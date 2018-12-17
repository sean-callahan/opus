package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.jvm.ConstantPool;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class AttributeParent extends Attribute
{
    private final static int MAX_BUFFER_SIZE = 1 << 12;

    private final ByteBuffer attributeBuffer = ByteBuffer.allocateDirect(MAX_BUFFER_SIZE);
    private final List<Attribute> attributes = new ArrayList<>();

    protected AttributeParent(ConstantPool pool, ByteBuffer buffer, String name)
    {
        super(pool, buffer, name);
    }

    public ByteBuffer getAttributeBuffer()
    {
        return attributeBuffer;
    }

    public List<Attribute> getAttributes()
    {
        return attributes;
    }
}
