package net.seancallahan.opus.compiler.parser;

import net.seancallahan.opus.compiler.Scope;

import java.io.DataOutputStream;
import java.io.IOException;
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

    public void writeTo(DataOutputStream out) throws IOException
    {
        out.writeInt(statements.size());
        for (Statement statement : statements)
        {
            if (statement != null)
            {
                statement.writeTo(out);
            }
        }
    }
}
