package net.seancallahan.opus.compiler;

import net.seancallahan.opus.compiler.parser.Body;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Variable;

import java.util.ArrayList;
import java.util.List;

public class Function implements Declaration
{
    private final Token name;

    private final List<Variable> parameters = new ArrayList<>();
    private final List<Variable> returns = new ArrayList<>();
    private final Body body;

    private final Scope scope;

    public Function(Token name, Scope parent)
    {
        this.name = name;
        this.scope = new Scope(parent);
        this.body = new Body(scope);
    }

    public Token getName()
    {
        return name;
    }

    public boolean isPublic()
    {
        return Character.isUpperCase(name.getValue().charAt(0));
    }

    public List<Variable> getParameters()
    {
        return parameters;
    }

    public List<Variable> getReturns()
    {
        return returns;
    }

    public Body getBody()
    {
        return body;
    }

    public Scope getScope()
    {
        return scope;
    }

    @Override
    public String toString()
    {
        return name.getValue();
    }
}
