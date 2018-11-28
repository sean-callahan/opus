package net.seancallahan.opus.lang;

import net.seancallahan.opus.compiler.Token;

public final class Variable implements Declaration
{
    private final Token name;
    private final Type type;

    public Variable(Token name, Type type)
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return name.getValue();
    }

    public Type getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return name.getValue() + " " + type;
    }

}
