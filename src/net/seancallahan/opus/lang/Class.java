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
        return Character.isUpperCase(name.getValue().charAt(0));
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(name.getValue()).append("{ ");

        for (int i = 0; i < fields.size(); i++)
        {
            sb.append(fields.get(i));
            if (i != fields.size() - 1)
            {
                sb.append(',');
            }
            sb.append(' ');
        }
        sb.append('}');

        return sb.toString();
    }

}
