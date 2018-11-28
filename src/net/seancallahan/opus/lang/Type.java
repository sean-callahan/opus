package net.seancallahan.opus.lang;

public class Type implements Declaration
{
    private final String name;

    private static final String[] primitives = new String[]
    {
        "bool", "string", "u8", "u16", "u32", "u64",
        "s8", "s16", "s32", "s64", "f32", "f64",
    };

    private static final int[] primitiveSizes = new int[]
    {
        1, // bool
        0, // string
        1, // u8
        2, // u16
        4, // u32
        8, // u64
        1, // s8
        2, // s16
        4, // s32
        8, // s64
        4, // f32
        8, // f64
    };

    private final int size;
    private final boolean primitive;

    public Type(String name)
    {
        this.name = name;

        int size = 0;

        boolean primitive = false;

        for (int i = 0; i < primitives.length; i++)
        {
            String primitiveName = primitives[i];
            if (primitiveName.equals(name))
            {
                primitive = true;
                size = primitiveSizes[i];
                break;
            }
        }

        this.size = size;
        this.primitive = primitive;
    }

    public String getName()
    {
        return name;
    }

    public int getSize()
    {
        return size;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public boolean isPrimitive()
    {
        return primitive;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Type))
        {
            return false;
        }

        return ((Type)obj).name.equals(name);
    }
}
