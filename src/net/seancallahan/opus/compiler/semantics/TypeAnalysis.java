package net.seancallahan.opus.compiler.semantics;

import net.seancallahan.opus.compiler.Function;
import net.seancallahan.opus.compiler.Scope;
import net.seancallahan.opus.compiler.Token;
import net.seancallahan.opus.compiler.TokenType;
import net.seancallahan.opus.compiler.parser.Expression;
import net.seancallahan.opus.compiler.parser.Parser;
import net.seancallahan.opus.compiler.parser.Statement;
import net.seancallahan.opus.compiler.parser.SyntaxException;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Type;
import net.seancallahan.opus.lang.Variable;

public class TypeAnalysis implements Resolver
{
    private final Parser parser;

    public TypeAnalysis(Parser parser)
    {
        this.parser = parser;
    }

    public void perform() throws SyntaxException
    {
        for (Declaration declaration : parser.getDeclarations().values())
        {
            if (declaration instanceof Function)
            {
                for (Statement statement : ((Function)declaration).getBody().getStatements())
                {
                    if (statement != null)
                    {
                        statement.resolve(this);
                    }
                }
            }
        }
    }

    private Type checkType(Expression expression) throws SyntaxException
    {
        if (expression instanceof Expression.Literal)
        {
            return checkLiteralType((Expression.Literal)expression);
        }
        if (expression instanceof Expression.Unary)
        {
            return checkType(((Expression.Unary)expression).getRight());
        }
        if (expression instanceof Expression.Group)
        {
            return checkType(((Expression.Group)expression).getInner());
        }
        if (expression instanceof Expression.Binary)
        {
            Expression.Binary binary = (Expression.Binary)expression;
            Type left = checkType(binary.getLeft());
            Type right = checkType(binary.getRight());

            if (!left.equals(right))
            {
                throw new SyntaxException("incompatible types in expression");
            }

            return left;
        }
        if (expression instanceof Expression.FunctionCall)
        {
            return null;
        }

        throw new UnsupportedOperationException("cannot check type of unsupported expression");
    }

    private Type checkLiteralType(Expression.Literal literal) throws SyntaxException
    {
        Token token = literal.getToken();
        String value = token.getValue();

        if (token.getType() == TokenType.NAME || token.getType() == TokenType.THIS)
        {
            Declaration declaration = literal.getScope().get(value);
            if (declaration instanceof Function)
            {
                return new Type(declaration.getName().getValue());
            }
            if (declaration instanceof Variable)
            {
                return ((Variable)declaration).getType();
            }
            if (!(declaration instanceof Statement.VariableDeclaration))
            {
                throw new UnsupportedOperationException();
            }

            Statement.VariableDeclaration varDecl = (Statement.VariableDeclaration) declaration;

            Variable variable = varDecl.getVariable();
            if (variable.getType() != null)
            {
                return variable.getType();
            }

            Type type = checkType(varDecl.getExpression());
            varDecl.getVariable().setType(type);

            return type;
        }

        if (value.equals("true") || value.equals("false"))
        {
            return new Type("bool");
        }

        // NOTE: there might be a better way to do this rather than just ignoring failures
        try
        {
            Integer.parseInt(value);
            return new Type("s32");
        }
        catch (NumberFormatException ignored)
        {
        }

        try
        {
            Long.parseLong(value);
            return new Type("s64");
        }
        catch (NumberFormatException ignored)
        {
        }

        try
        {
            Float.parseFloat(value);
            return new Type("f32");
        }
        catch (NumberFormatException ignored)
        {
        }

        return new Type("string");
    }

    @Override
    public Object resolve(Expression expression) throws SyntaxException
    {
        return checkType(expression);
    }

    @Override
    public Object resolve(Scope scope, Token token) throws SyntaxException
    {
        return null;
    }
}
