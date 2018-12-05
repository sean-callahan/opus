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
        for (Constant in : pool)
        {
            if (in.getValue().equals(item.getValue()))
            {
                return (short)(pool.indexOf(item)+1);
            }
        }

        pool.add(item);
        return (short)(pool.indexOf(item)+1);
    }

    public Constant get(int index)
    {
        return pool.get(index);
    }

    public void write(DataOutputStream out) throws IOException
    {
        out.writeShort(pool.size()+1);

        for (Constant c : pool)
        {
            if (c != null)
            {
                c.write(out);
            }
        }
    }

}
