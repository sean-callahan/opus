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
        for (short index = 0; index < pool.size(); index++)
        {
            Constant constant = pool.get(index);
            if (constant.getKind() == item.getKind() && constant.getValue().equals(item.getValue()))
            {
                return (short)(index+1);
            }
        }
        pool.add(item);
        return (short)pool.size();
    }

    public Constant get(int index)
    {
        return pool.get(index-1);
    }

    public short search(Constant constant)
    {
        return (short)(pool.indexOf(constant)+1);
    }

    public short search(Constant.Kind kind, Object value)
    {
        for (short i = 0; i < pool.size(); i++)
        {
            Constant constant = pool.get(i);
            if (constant.getKind() != kind)
            {
                continue;
            }
            if (constant.getValue().equals(value))
            {
                return (short)(i+1);
            }
        }
        return -1;
    }

    public void write(DataOutputStream out) throws IOException
    {
        out.writeShort((short)pool.size());
        for (Constant c : pool)
        {
            c.write(out);
        }
    }

}
