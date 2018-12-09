package net.seancallahan.opus.lang;

import net.seancallahan.opus.compiler.Token;

public class Field implements Member
{
    private final Class parent;
    private final Variable field;

    public Field(Class parent, Variable field)
    {
        this.parent = parent;
        this.field = field;
    }

    public Class getParent()
    {
        return parent;
    }

    public Variable getField()
    {
        return field;
    }

    @Override
    public Token getName()
    {
        return field.getName();
    }
}
