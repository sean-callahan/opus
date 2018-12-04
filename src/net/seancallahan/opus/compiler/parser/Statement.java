package net.seancallahan.opus.compiler.parser;

import net.seancallahan.opus.compiler.Scope;
import net.seancallahan.opus.compiler.Token;
import net.seancallahan.opus.compiler.TokenType;
import net.seancallahan.opus.compiler.semantics.Resolvable;
import net.seancallahan.opus.compiler.semantics.Resolver;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Type;
import net.seancallahan.opus.lang.Variable;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Statement implements Resolvable
{
    private final Body parent;

    public Statement(Body parent)
    {
        this.parent = parent;
    }

    public Body getParent()
    {
        return parent;
    }

    public static Statement parse(ParserContext context) throws SyntaxException
    {
        Token next = context.getIterator().next();

        switch (next.getType())
        {
            case NAME:
                return simple(context, next);
            case IF:
                return parseIf(context);
            case FOR:
                return parseFor(context);
            case RETURN:
                return parseReturn(context);
            default:
                return null;
        }
    }

    private static VariableDeclaration parseVariableDeclaration(ParserContext context, Token name) throws SyntaxException
    {
        Declaration previous = getPreviousDeclaration(context, name);
        if (previous != null)
        {
            throw new SyntaxException("symbol already declared", previous.getName());
        }

        Type type = Parser.parseType(context);

        context.expect(TokenType.TERMINATOR);

        VariableDeclaration declaration = new VariableDeclaration(context.getCurrentBody(), name, type);

        context.getCurrentBody().getScope().add(declaration);

        return declaration;
    }

    private static VariableDeclaration parseVariableDefine(ParserContext context, Token name) throws SyntaxException
    {
        Declaration previous = getPreviousDeclaration(context, name);
        if (previous != null)
        {
            throw new SyntaxException("symbol already declared", previous.getName());
        }

        Expression expression = Expression.parse(context);

        context.expect(TokenType.TERMINATOR);

        VariableDeclaration declaration = new VariableDeclaration(context.getCurrentBody(), name, expression);

        context.getCurrentBody().getScope().add(declaration);

        return declaration;
    }

    private static If parseIf(ParserContext context) throws SyntaxException
    {
        Expression condition = Expression.parse(context);

        Body body = parseBody(context);

        return new If(context.getCurrentBody(), condition, body);
    }

    private static For parseFor(ParserContext context) throws SyntaxException
    {
        if (context.matches(TokenType.LEFT_BRACE))
        {
            // "for"ever loop
            Body body = parseBody(context);

            return new For(context.getCurrentBody(), body);
        }

        Expression condition = Expression.parse(context);

        Body body = parseBody(context);

        return new For(context.getCurrentBody(), condition, body);
    }

    public static Constant parseConstant(ParserContext context, Token name) throws SyntaxException
    {
        // TODO: check scope for duplicates

        Type type = Parser.parseType(context);

        context.expect(TokenType.ASSIGN);

        Expression value = Expression.parse(context);

        context.expect(TokenType.TERMINATOR);

        return new Constant(context.getCurrentBody(), name, type, value);
    }

    public static Import parseImport(ParserContext context) throws SyntaxException
    {
        Token path = context.expect(TokenType.LITERAL);
        return new Import(context.getCurrentBody(), path);
    }

    private static Return parseReturn(ParserContext context) throws SyntaxException
    {
        // TODO: multi returns
        Expression value = Expression.parse(context);
        return new Return(context.getCurrentBody(), value);
    }

    private static Statement simple(ParserContext context, Token name) throws SyntaxException
    {
        if (context.has(TokenType.DOT))
        {
            return new SimpleExpression(context.getCurrentBody(), Expression.member(context, name));
        }
        else if (context.has(TokenType.LEFT_PAREN))
        {
            return new SimpleExpression(context.getCurrentBody(), Expression.method(context, null, name));
        }

        Token next = context.getIterator().next();
        switch (next.getType())
        {
            case ASSIGN:
                if (!symbolExists(context, name))
                {
                    throw new SyntaxException("cannot assign to undeclared variable", name);
                }
                Expression value = Expression.parse(context);
                context.expect(TokenType.TERMINATOR);
                return new Assignment(context.getCurrentBody(), name, value);
            case DECLARE:
                return parseVariableDeclaration(context, name);
            case DEFINE:
                return parseVariableDefine(context, name);
            case DECLARE_GLOBAL:
                throw new UnsupportedOperationException("cannot declare a global type from within a function");
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static Body parseBody(ParserContext context) throws SyntaxException
    {
        context.expect(TokenType.LEFT_BRACE);

        Body parent = context.getCurrentBody();

        Body body = new Body(parent.getScope().copy());

        context.setCurrentBody(body);

        while (!context.has(TokenType.RIGHT_BRACE))
        {
            body.getStatements().add(Statement.parse(context));
        }

        context.setCurrentBody(parent);

        return body;
    }

    private static boolean symbolExists(ParserContext context, Token name)
    {
        return getPreviousDeclaration(context, name) != null;
    }

    private static Declaration getPreviousDeclaration(ParserContext context, Token name)
    {
        Scope scope = context.getCurrentBody().getScope();
        if (scope == null)
        {
            return null;
        }
        return scope.get(name.getValue());
    }

    public void writeTo(DataOutputStream out) throws IOException
    {
        out.writeUTF(getClass().getSimpleName());
    }

    @Override
    public void resolve(Resolver resolver) throws SyntaxException
    {

    }

    public static class Assignment extends Statement
    {
        private Token name;
        private Expression expression;

        private Assignment(Body parent, Token name, Expression expression)
        {
            super(parent);
            this.name = name;
            this.expression = expression;
        }

        public Token getName()
        {
            return name;
        }

        public Expression getExpression()
        {
            return expression;
        }

        @Override
        public void writeTo(DataOutputStream out) throws IOException
        {
            super.writeTo(out);
            name.writeTo(out);
            expression.writeTo(out);
        }

        @Override
        public void resolve(Resolver resolver) throws SyntaxException
        {
            resolver.resolve(getParent().getScope(), name);
            resolver.resolve(expression);
        }
    }

    public static class Constant extends Statement implements Declaration
    {
        private final Token name;
        private final Type type;
        private final Expression value;

        private Constant(Body parent, Token name, Type type, Expression value)
        {
            super(parent);
            this.name = name;
            this.type = type;
            this.value = value;
        }

        public Type getType()
        {
            return type;
        }

        public Expression getValue()
        {
            return value;
        }

        @Override
        public Token getName()
        {
            return name;
        }

        @Override
        public void resolve(Resolver resolver) throws SyntaxException
        {
            resolver.resolve(getParent().getScope(), name);
            resolver.resolve(value);
        }
    }

    public static class Return extends Statement
    {
        private Expression expression;

        private Return(Body parent, Expression expression)
        {
            super(parent);
            this.expression = expression;
        }

        public Expression getExpression()
        {
            return expression;
        }

        @Override
        public void resolve(Resolver resolver) throws SyntaxException
        {
            resolver.resolve(expression);
        }
    }

    public static class Import extends Statement implements Declaration
    {
        private final Token path;

        private Import(Body parent, Token path)
        {
            super(parent);
            this.path = path;
        }

        public Token getPath()
        {
            return path;
        }

        @Override
        public Token getName()
        {
            return path;
        }
    }

    public static class For extends Statement
    {
        private Assignment index;
        private Expression condition;
        private Statement counter;
        private Body body;

        private For(Body parent, Body body)
        {
            super(parent);
            this.body = body;
        }

        private For(Body parent, Expression condition, Body body)
        {
            super(parent);
            this.condition = condition;
            this.body = body;
        }

        protected For(Body parent, Assignment index, Expression condition, Statement counter, Body body)
        {
            super(parent);
            this.index = index;
            this.condition = condition;
            this.counter = counter;
            this.body = body;
        }

        public Assignment getIndex()
        {
            return index;
        }

        public Expression getCondition()
        {
            return condition;
        }

        public Statement getCounter()
        {
            return counter;
        }

        public Body getBody()
        {
            return body;
        }

        @Override
        public void resolve(Resolver resolver) throws SyntaxException
        {
            if (index != null)
            {
                index.resolve(resolver);
            }
            if (condition != null)
            {
                condition.resolve(resolver);
            }
            if (counter != null)
            {
                counter.resolve(resolver);
            }
            // TODO: body
        }
    }

    public static class If extends Statement
    {
        private Expression condition;
        private Body body;

        private If(Body parent, Expression condition, Body body)
        {
            super(parent);
            this.condition = condition;
            this.body = body;
        }

        public Expression getCondition()
        {
            return condition;
        }

        public Body getBody()
        {
            return body;
        }

        @Override
        public void resolve(Resolver resolver) throws SyntaxException
        {
            resolver.resolve(condition);
            // TODO: body
        }
    }

    public static class VariableDeclaration extends Statement implements Declaration
    {
        private final Variable variable;
        private final Expression expression;

        private VariableDeclaration(Body parent, Token name, Type type)
        {
            super(parent);
            this.variable = new Variable(name, type);
            this.expression = null;
        }

        private VariableDeclaration(Body parent, Token name, Expression expression)
        {
            super(parent);
            this.variable = new Variable(name);
            this.expression = expression;
        }

        @Override
        public Token getName()
        {
            return variable.getName();
        }

        public Variable getVariable()
        {
            return variable;
        }

        public Expression getExpression()
        {
            return expression;
        }

        @Override
        public void resolve(Resolver resolver) throws SyntaxException
        {
            if (expression != null)
            {
                Object value = resolver.resolve(expression);
                if (value instanceof Type)
                {
                    variable.setType((Type)value);
                }
            }
        }
    }

    public static class SimpleExpression extends Statement
    {
        private final Expression expression;

        public SimpleExpression(Body parent, Expression expression)
        {
            super(parent);
            this.expression = expression;
        }

        public Expression getExpression()
        {
            return expression;
        }

        @Override
        public void resolve(Resolver resolver) throws SyntaxException
        {
            resolver.resolve(expression);
        }
    }
}
