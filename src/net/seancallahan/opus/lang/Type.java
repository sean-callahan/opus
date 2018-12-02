package net.seancallahan.opus.lang;

import net.seancallahan.opus.compiler.Token;

public class Type implements Declaration
{
    private final Token name;

    private final boolean primitive;
    private static final String[] primitives = new String[]
    {
        "bool", "string", "u8", "u16", "u32", "u64",
        "s8", "s16", "s32", "s64", "f32", "f64",
    };

    public Type(Token name)
    {
        this.name = name;

        boolean primitive = false;

        for (String primitiveName : primitives)
        {
            if (primitiveName.equals(name.getValue()))
            {
                primitive = true;
                break;
            }
        }

        this.primitive = primitive;
    }

    public Token getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name.getValue();
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
