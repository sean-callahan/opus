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
        Type type;

        if (expression instanceof Expression.Literal)
        {
            type = checkLiteralType((Expression.Literal)expression);
        }
        else if (expression instanceof Expression.Unary)
        {
            type = checkType(((Expression.Unary)expression).getRight());
        }
        else if (expression instanceof Expression.Group)
        {
            type = checkType(((Expression.Group)expression).getInner());
        }
        else if (expression instanceof Expression.Binary)
        {
            Expression.Binary binary = (Expression.Binary)expression;

            Expression left = binary.getLeft();
            Expression right = binary.getRight();

            Type leftType = checkType(left);
            Type rightType = checkType(right);


            if (left instanceof Expression.Literal && right instanceof Expression.Literal)
            {
                if (!leftType.equals(rightType))
                {
                    throw new SyntaxException("incompatible types in expression", ((Expression.Literal) left).getToken());
                }
                type = new Type("bool");
            }
            else
            {
                if (!(leftType.getName().equals("bool") && rightType.getName().equals("bool")))
                {
                    throw new SyntaxException("can only make comparisons with two boolean expressions");
                }
                type = leftType;
            }
        }
        else if (expression instanceof Expression.FunctionCall)
        {
            type = null;
        }
        else
        {
            throw new UnsupportedOperationException("cannot check type of unsupported expression");
        }

        expression.setType(type);
        return type;
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
