package net.seancallahan.opus.lang;

import net.seancallahan.opus.compiler.Function;
import net.seancallahan.opus.compiler.Scope;
import net.seancallahan.opus.compiler.Token;

public final class Method extends Function implements Member
{
    private Class parent;

    private final Token parentName;
    private final boolean _static;

    public Method(Token parentName, Token name, Scope global, boolean _static)
    {
        super(name, global);
        this.parentName = parentName;
        this._static = _static;

        getScope().put("this", null);
    }

    public Method(Function function, Class parent, boolean _static)
    {
        super(function);
        this.parentName = parent.getName();
        this.parent = parent;
        this._static = _static;
    }

    public Class getParent()
    {
        return parent;
    }

    public boolean isStatic()
    {
        return _static;
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
