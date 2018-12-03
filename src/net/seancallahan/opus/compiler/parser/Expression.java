package net.seancallahan.opus.compiler.parser;

import net.seancallahan.opus.compiler.Operator;
import net.seancallahan.opus.compiler.Token;
import net.seancallahan.opus.compiler.TokenType;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Expression
{
    public static Expression parse(ParserContext context) throws SyntaxException
    {
        return equals(context);
    }

    private static Expression equals(ParserContext context) throws SyntaxException
    {
        Expression left = comparison(context);

        while (context.matches(Operator.NEQ, Operator.EQ))
        {
            Operator operator = context.getIterator().next().getOperator();
            Expression right = comparison(context);
            left = new Binary(left, operator, right);
        }

        return left;
    }

    private static Expression comparison(ParserContext context) throws SyntaxException
    {
        Expression left = addition(context);

        while (context.matches(Operator.GT, Operator.GEQ, Operator.LT, Operator.LEQ, Operator.AND, Operator.OR))
        {
            Operator operator = context.getIterator().next().getOperator();
            Expression right = addition(context);
            left = new Binary(left, operator, right);
        }

        return left;
    }

    private static Expression addition(ParserContext context) throws SyntaxException
    {
        Expression left = multiplication(context);

        while (context.matches(Operator.ADD, Operator.SUBTRACT))
        {
            Operator operator = context.getIterator().next().getOperator();
            Expression right = multiplication(context);
            left = new Binary(left, operator, right);
        }

        return left;
    }

    private static Expression multiplication(ParserContext context) throws SyntaxException
    {
        Expression left = unary(context);

        while (context.matches(Operator.DIVIDE, Operator.MULTIPLY, Operator.MOD))
        {
            Operator operator = context.getIterator().next().getOperator();
            Expression right = unary(context);
            left = new Binary(left, operator, right);
        }

        return left;
    }

    private static Expression unary(ParserContext context) throws SyntaxException
    {
        if (context.matches(Operator.NOT))
        {
            Operator operator = context.getIterator().next().getOperator();
            Expression right = unary(context);
            return new Unary(operator, right);
        }

        return primary(context);
    }

    private static Expression primary(ParserContext context) throws SyntaxException
    {
        if (context.matches(TokenType.NAME, TokenType.THIS))
        {
            Token name = context.getIterator().next();

            if (context.has(TokenType.DOT))
            {
                return member(context, name);
            }
            else if (context.has(TokenType.LEFT_BRACE))
            {
                return createNew(context, name);
            }

            return new Literal(name);
        }

        if (context.matches(TokenType.LITERAL,TokenType.NIL, TokenType.TRUE, TokenType.FALSE))
        {
            return new Literal(context);
        }

        if (context.matches(TokenType.LEFT_PAREN))
        {
            context.getIterator().skip(1);
            Expression group = parse(context);
            context.expect(TokenType.RIGHT_PAREN);
            return new Group(group);
        }

        return null;
    }

    private static Expression createNew(ParserContext context, Token clazz) throws SyntaxException
    {
        Map<Token, Expression> fields = new HashMap<>();

        if (!context.has(TokenType.RIGHT_BRACE))
        {
            do
            {
                Token field = context.expect(TokenType.NAME);
                context.expect(TokenType.DECLARE);

                Expression expression = parse(context);

                fields.put(field, expression);
            } while (context.has(TokenType.COMMA));
            context.expect(TokenType.RIGHT_BRACE);
        }

        return new Instantiation(clazz, fields);
    }

    private static Expression member(ParserContext context, Token callee) throws SyntaxException
    {
        Token name = context.expect(TokenType.NAME);

        if (context.has(TokenType.LEFT_PAREN))
        {
            return method(context, callee, name);
        }

        return new FieldReference(callee, name);
    }

    private static Expression method(ParserContext context, Token callee, Token name) throws SyntaxException
    {
        List<Expression> arguments = new ArrayList<>();

        if (!context.has(TokenType.RIGHT_PAREN))
        {
            do
            {
                arguments.add(parse(context));
            } while (context.has(TokenType.COMMA));
            context.expect(TokenType.RIGHT_PAREN);
        }

        return new FunctionCall(callee, name, arguments);
    }

    public abstract void print();

    public void writeTo(DataOutputStream out) throws IOException
    {
        // NOTE: maybe use an enum to store expression types.
        out.writeUTF(getClass().getName());
    }

    public static final class Binary extends Expression
    {
        private final Expression left;
        private final Operator operator;
        private final Expression right;

        private Binary(Expression left, Operator operator, Expression right)
        {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expression getLeft()
        {
            return left;
        }

        public Operator getOperator()
        {
            return operator;
        }

        public Expression getRight()
        {
            return right;
        }

        @Override
        public void print()
        {
            /*System.out.println("   " + operator);

            if (left != null)
            {
                System.out.print(" / ");
            }
            if (right != null)
            {
                System.out.print("  \\");
            }
            System.out.println();

            if (left != null)
            {
                left.print();
                System.out.print("  ");
            }

            if (right != null)
            {
                System.out.print("   ");
                right.print();
            }

            System.out.println();*/
        }

        @Override
        public void writeTo(DataOutputStream out) throws IOException
        {
            super.writeTo(out);
            getOperator().writeTo(out);
            getLeft().writeTo(out);
            getRight().writeTo(out);
        }
    }

    public static final class FieldReference extends Expression
    {
        private final Token callee;
        private final Token name;

        private FieldReference(Token callee, Token name)
        {
            this.callee = callee;
            this.name = name;
        }

        public Token getCallee()
        {
            return callee;
        }

        public Token getName()
        {
            return name;
        }

        @Override
        public void print()
        {

        }

        @Override
        public void writeTo(DataOutputStream out) throws IOException
        {
            super.writeTo(out);
            getCallee().writeTo(out);
            getName().writeTo(out);
        }
    }

    public static final class FunctionCall extends Expression
    {
        private final Token callee;
        private final Token function;
        private final List<Expression> arguments;

        private FunctionCall(Token callee, Token name, List<Expression> arguments)
        {
            this.callee = callee;
            this.function = name;
            this.arguments = arguments;
        }

        public boolean isMethod()
        {
            return callee != null;
        }

        public Token getCallee()
        {
            return callee;
        }

        public Token getFunction()
        {
            return function;
        }

        public List<Expression> getArguments()
        {
            return arguments;
        }

        @Override
        public void print()
        {

        }

        @Override
        public void writeTo(DataOutputStream out) throws IOException
        {
            super.writeTo(out);

            getCallee().writeTo(out);
            getFunction().writeTo(out);

            out.writeShort(getArguments().size());
            for (Expression arg : getArguments())
            {
                arg.writeTo(out);
            }
        }
    }

    public static final class Group extends Expression
    {
        private Expression inner;

        private Group(Expression inner)
        {
            this.inner = inner;
        }

        @Override
        public void print()
        {
            if (inner != null)
            {
                inner.print();
            }
        }

        @Override
        public void writeTo(DataOutputStream out) throws IOException
        {
            super.writeTo(out);
            inner.writeTo(out);
        }
    }

    public static final class Literal extends Expression
    {
        private Token token;

        private Literal(Token token)
        {
            this.token = token;
        }

        private Literal(ParserContext context)
        {
            this.token = context.getIterator().next();
        }

        public Token getToken()
        {
            return token;
        }

        @Override
        public void print()
        {
            System.out.print(token.getValue());
        }

        @Override
        public void writeTo(DataOutputStream out) throws IOException
        {
            super.writeTo(out);
            token.writeTo(out);
        }
    }

    public static final class Instantiation extends Expression
    {
        private final Token className;
        private final Map<Token, Expression> fields;

        private Instantiation(Token className, Map<Token, Expression> fields)
        {
            this.className = className;
            this.fields = fields;
        }

        public Token getClassName()
        {
            return className;
        }

        public Map<Token, Expression> getFields()
        {
            return fields;
        }

        @Override
        public void print()
        {

        }

        @Override
        public void writeTo(DataOutputStream out) throws IOException
        {
            super.writeTo(out);
            getClassName().writeTo(out);

            out.writeShort(getFields().size());
            for (Token token : getFields().keySet())
            {
                token.writeTo(out);
                getFields().get(token).writeTo(out);
            }
        }
    }

    public static final class Unary extends Expression
    {
        private final Operator operator;
        private final Expression right;

        private Unary(Operator operator, Expression right)
        {
            this.operator = operator;
            this.right = right;
        }
        public Operator getOperator()
        {
            return operator;
        }

        public Expression getRight()
        {
            return right;
        }

        @Override
        public void print()
        {
            System.out.print(operator);

            if (right != null)
            {
                System.out.print("  \\");
            }
            System.out.println();

            if (right != null)
            {
                right.print();
            }
        }

        @Override
        public void writeTo(DataOutputStream out) throws IOException
        {
            super.writeTo(out);
            getOperator().writeTo(out);
            getRight().writeTo(out);
        }
    }
}
