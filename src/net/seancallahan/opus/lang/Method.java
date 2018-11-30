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
    }

    public Class getParent()
    {
        return parent;
    }

    public void setParent(Class parent)
    {
        this.parent = parent;
    }

    public String getParentName()
    {
        return parentName.getValue();
    }

    @Override
    public String toString()
    {
        return String.format("%s.%s", getParentName(), super.toString());
    }

}
