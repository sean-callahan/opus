package net.seancallahan.opus.lang;

import net.seancallahan.opus.compiler.Token;

import java.util.ArrayList;
import java.util.List;

public final class Class implements Declaration
{
    private final Token name;

    private final List<Variable> fields = new ArrayList<>();
    private final List<Method> methods = new ArrayList<>();

    public Class(Token name)
    {
        this.name = name;
    }

    public Token getName()
    {
        return name;
    }

    public List<Variable> getFields()
    {
        return fields;
    }

    public List<Method> getMethods()
    {
        return methods;
    }

    public boolean isPublic()
    {
        if (name == null)
        {
            return true;
        }

        return Character.isUpperCase(name.getValue().charAt(0));
    }

}
