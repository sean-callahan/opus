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

    public boolean contains(String name)
    {
        return members.containsKey(name) || (parent != null && parent.members.containsKey(name));
    }

    public void put(String key, Declaration declaration)
    {
        members.put(key, declaration);
    }

    public boolean add(String key, Declaration declaration)
    {
        if (members.containsKey(key))
        {
            return false;
        }
        members.put(key, declaration);
        return true;
    }

    public boolean add(Declaration declaration)
    {
        return add(declaration.getName().getValue(), declaration);
    }

    public Scope copy()
    {
        Scope scope = new Scope(parent);
        scope.members.putAll(members);
        return scope;
    }

    public static Scope childOf(Scope other)
    {
        Scope scope = new Scope(other);
        scope.members.putAll(other.members);
        return scope;
    }

}
