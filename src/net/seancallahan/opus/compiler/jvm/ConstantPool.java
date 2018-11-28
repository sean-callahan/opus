package net.seancallahan.opus.compiler.jvm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConstantPool
{
    private final List<Constant> pool = new ArrayList<>();

    public short add(Constant item)
    {
        pool.add(item);
        return (short)pool.indexOf(item);
    }

    public Constant get(int index)
    {
        return pool.get(index);
    }

    public void write(DataOutputStream out) throws IOException
    {
        out.writeShort(pool.size());

        for (Constant c : pool)
        {
            if (c != null)
            {
                c.write(out);
            }
        }
    }

}
