package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.jvm.Constant;
import net.seancallahan.opus.compiler.jvm.ConstantPool;

import java.io.File;
import java.nio.ByteBuffer;

public class SourceFile extends Attribute
{
    private final short nameIndex;

    public SourceFile(ConstantPool pool, ByteBuffer buffer, File file)
    {
        super(pool, buffer, "SourceFile");

        nameIndex = pool.add(new Constant.UTF8(pool, file.getName()));
    }

    @Override
    public void write(ByteBuffer out)
    {
        body.putShort(nameIndex);
        super.write(out);
    }
}
