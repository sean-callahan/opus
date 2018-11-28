package net.seancallahan.opus.compiler;

import net.seancallahan.opus.compiler.parser.Statement;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Variable;

import java.util.ArrayList;
import java.util.List;

public class Function implements Declaration
{
    private final Token name;

    private final List<Variable> parameters = new ArrayList<>();
    private final List<Variable> returns = new ArrayList<>();
    private final List<Statement> body = new ArrayList<>();

    private final Scope scope;

    public Function(Token name, Scope parent)
    {
        this.name = name;
        this.scope = new Scope(parent);
    }

    public String getName()
    {
        return name.getValue();
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

    public List<Statement> getBody()
    {
        return body;
    }

    public Scope getScope()
    {
        return scope;
    }

}
