package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.jvm.ConstantPool;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class LineNumberTable extends Attribute
{
    private final HashMap<Short, Short> lineNumbers = new HashMap<>();

    public LineNumberTable(ConstantPool pool, ByteBuffer buffer)
    {
        super(pool, buffer, "LineNumberTable");
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
    public void write(ByteBuffer out)
    {
        body.putShort((short)lineNumbers.size());
        for (short lineNumber : lineNumbers.keySet())
        {
            short startPc = lineNumbers.get(lineNumber);
            body.putShort(startPc);
            body.putShort(lineNumber);
        }

        super.write(out);
    }
}
