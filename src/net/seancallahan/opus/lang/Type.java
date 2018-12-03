package net.seancallahan.opus.lang;

import net.seancallahan.opus.compiler.Token;

public class Type
{
    private final String name;

    private final boolean primitive;
    private static final String[] primitives = new String[]
    {
        "bool", "string", "u8", "u16", "u32", "u64",
        "s8", "s16", "s32", "s64", "f32", "f64",
    };

    public Type(Token name)
    {
        this(name.getValue());
    }

    public Type(String name)
    {
        this.name = name;

        boolean primitive = false;

        for (String primitiveName : primitives)
        {
            if (primitiveName.equals(name))
            {
                primitive = true;
                break;
            }
        }

        this.primitive = primitive;
    }

    public String getName()
    {
        return name;
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
