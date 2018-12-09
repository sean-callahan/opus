package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.jvm.Constant;
import net.seancallahan.opus.compiler.jvm.ConstantPool;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class SourceFile extends Attribute
{
    private final short nameIndex;

    public SourceFile(ConstantPool pool, File file)
    {
        super(pool, "SourceFile");

        nameIndex = pool.add(new Constant.UTF8(pool, file.getName()));
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        buffer.writeShort(nameIndex);
        super.write(out);
    }
}
