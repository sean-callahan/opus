package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.jvm.ConstantPool;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class LineNumberTable extends Attribute
{
    private final HashMap<Short, Short> lineNumbers = new HashMap<>();

    public LineNumberTable(ConstantPool pool)
    {
        super(pool, "LineNumberTable");
    }

    public void add(short lineNumber, short codePosition)
    {
        lineNumbers.put(lineNumber, codePosition);
    }

    public boolean contains(short lineNumber)
    {
        return lineNumbers.containsKey(lineNumber);
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        buffer.writeShort(lineNumbers.size());
        for (Short lineNumber : lineNumbers.keySet())
        {
            short startPc = lineNumbers.get(lineNumber);
            buffer.writeShort(startPc);
            buffer.write(lineNumber);
        }

        super.write(out);
    }
}
