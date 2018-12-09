package net.seancallahan.opus.lang;

import net.seancallahan.opus.compiler.Function;
import net.seancallahan.opus.compiler.Scope;
import net.seancallahan.opus.compiler.Token;

public final class Method extends Function
{
    private Class parent;

    private final Token parentName;

    public Method(Token parentName, Token name, Scope global)
    {
        super(name, global);
        this.parentName = parentName;

        getScope().put("this", null);
    }

    public Method(Function function, Class parent)
    {
        super(function.getName(), function.getScope(), function.getBody());
        this.parentName = parent.getName();
        this.parent = parent;
    }

    public Class getParent()
    {
        return parent;
    }

    public void setParent(Class parent)
    {
        this.parent = parent;
        getScope().put("this", parent);
    }

    public Token getParentName()
    {
        return parentName;
    }

    @Override
    public String toString()
    {
        String parent = "[static]";
        if (getParentName() != null)
        {
            parent = getParentName().getValue();
        }
        return String.format("%s.%s", parent, super.toString());
    }

}
