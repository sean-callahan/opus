package net.seancallahan.opus.compiler.semantics;

import net.seancallahan.opus.compiler.Function;
import net.seancallahan.opus.compiler.Scope;
import net.seancallahan.opus.compiler.Token;
import net.seancallahan.opus.compiler.TokenType;
import net.seancallahan.opus.compiler.parser.Body;
import net.seancallahan.opus.compiler.parser.Expression;
import net.seancallahan.opus.compiler.parser.Parser;
import net.seancallahan.opus.compiler.parser.Statement;
import net.seancallahan.opus.compiler.parser.SyntaxException;
import net.seancallahan.opus.lang.Class;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Method;

public class ReferenceResolver implements Resolver
{
    private final Parser parser;

    public ReferenceResolver(Parser parser)
    {
        this.parser = parser;
    }

    public void resolve() throws SyntaxException
    {
        for (Declaration declaration : parser.getDeclarations().values())
        {
            if (declaration instanceof Method)
            {
                resolveMethod((Method)declaration);
            }
            else if (declaration instanceof Function)
            {
                resolveBody(((Function)declaration).getBody());
            }
        }
    }

    private void resolveMethod(Method method) throws SyntaxException
    {
        String parentName = method.getParentName().getValue();

        Declaration declaration = parser.getDeclarations().get(parentName);
        if (declaration == null)
        {
            throw new SyntaxException(String.format("no class exists with the name '%s'", parentName), method.getParentName());
        }
        else if (!(declaration instanceof Class))
        {
            throw new SyntaxException(String.format("cannot give method to non-class '%s'", parentName), method.getParentName());
        }

        Class clazz = (Class)declaration;

        method.setParent(clazz);
        clazz.getMethods().add(method);

        resolveBody(method.getBody());
    }

    public void resolveBody(Body body) throws SyntaxException
    {
        for (Statement statement : body.getStatements())
        {
            if (statement != null)
            {
                statement.resolve(this);
            }
        }
    }

    @Override
    public void resolve(Scope scope, Expression expression) throws SyntaxException
    {
        expression.resolve(this);
    }

    @Override
    public void resolve(Scope scope, Token token) throws SyntaxException
    {
        if (!(token.getType() == TokenType.NAME || token.getType() == TokenType.THIS))
        {
            return;
        }

        if (!scope.contains(token.getValue()))
        {
            throw new SyntaxException(String.format("'%s' is not defined", token.getValue()), token);
        }
    }
}
