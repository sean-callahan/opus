package net.seancallahan.opus.lang;

import net.seancallahan.opus.compiler.Token;

public final class Variable implements Declaration
{
    private final Token name;
    private final Type type;

    public Variable(Type type)
    {
        this(null, type);
    }

    public Variable(Token name, Type type)
    {
        this.name = name;
        this.type = type;
    }

    public Token getName()
    {
        return name;
    }

    public Type getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        if (name == null)
        {
            return type.getName().getValue();
        }
        return name.getValue() + " " + type;
    }

}
