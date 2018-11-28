package net.seancallahan.opus.util;

import java.util.Iterator;
import java.util.List;

public class PeekableIterator<T> implements Iterator<T>
{
    private final List<T> source;

    private int next = 0;

    public PeekableIterator(List<T> source)
    {
        this.source = source;
    }

    @Override
    public boolean hasNext()
    {
        return next < source.size();
    }

    @Override
    public T next()
    {
        T value = peek();
        next++;
        return value;
    }

    public T peek()
    {
        if (!hasNext())
        {
            return null;
        }
        return source.get(next);
    }

    public void skip(int n)
    {
        if (n < 0)
        {
            throw new IllegalArgumentException("can only skip a positive amount");
        }
        next += n;
    }

    @Override
    public void remove()
    {
        source.remove(next);
    }

}
