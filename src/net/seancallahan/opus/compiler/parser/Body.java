package net.seancallahan.opus.compiler.parser;

import net.seancallahan.opus.compiler.Scope;

import java.util.ArrayList;
import java.util.List;

public class Body
{
    private final Scope scope;
    private final List<Statement> statements = new ArrayList<>();

    public Body(Scope parent)
    {
        this.scope = new Scope(parent);
    }

    public Scope getScope()
    {
        return scope;
    }

    public List<Statement> getStatements()
    {
        return statements;
    }
}
