package net.seancallahan.opus.compiler;

import net.seancallahan.opus.lang.Declaration;

import java.util.HashMap;
import java.util.Map;

public class Scope
{
    private final Scope parent;
    private final Map<String, Declaration> members = new HashMap<>();

    public Scope(Scope parent)
    {
        this.parent = parent;
    }

    public Scope getParent()
    {
        return parent;
    }

    public Declaration get(Token token)
    {
        if (token.getType() != TokenType.NAME)
        {
            throw new IllegalArgumentException("token must have a NAME type");
        }
        return get(token.getValue());
    }

    public Declaration get(String name)
    {
        if (members.containsKey(name))
        {
            return members.get(name);
        }
        if (parent != null)
        {
            return parent.get(name);
        }
        return null;
    }

    public boolean add(Declaration declaration)
    {
        if (members.containsKey(declaration.getName()))
        {
            return false;
        }
        members.put(declaration.getName(), declaration);
        return true;
    }

}
