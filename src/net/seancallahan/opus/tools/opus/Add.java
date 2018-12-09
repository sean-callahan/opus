package net.seancallahan.opus.tools.opus;

public class Add
{
    private int a;
    private int b;

    public Add(int a, int b)
    {
        this.a = 0x53;
        this.b = b;
        int c = a;

        if ((b < c) || (a == b) || (b == c))
        {
            a = 5;
        }
    }

    public int process()
    {
        return a + b;
    }
}
