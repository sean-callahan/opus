package net.seancallahan.opus.compiler.parser;

import net.seancallahan.opus.compiler.Scope;
import net.seancallahan.opus.compiler.Token;
import net.seancallahan.opus.compiler.TokenType;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Type;
import net.seancallahan.opus.lang.Variable;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Statement
{
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

        VariableDeclaration declaration = new VariableDeclaration(name, type);

        context.getCurrentScope().add(declaration);

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

        Type type = Parser.parseType(expression);

        context.expect(TokenType.TERMINATOR);

        VariableDeclaration declaration = new VariableDeclaration(name, type, expression);

        context.getCurrentScope().add(declaration);

        return declaration;
    }

    private static If parseIf(ParserContext context) throws SyntaxException
    {
        Expression condition = Expression.parse(context);

        Body body = parseBody(context);

        return new If(condition, body);
    }

    private static For parseFor(ParserContext context) throws SyntaxException
    {
        if (context.matches(TokenType.LEFT_BRACE))
        {
            // "for"ever loop
            Body body = parseBody(context);

            return new For(body);
        }

        Expression condition = Expression.parse(context);

        Body body = parseBody(context);

        return new For(condition, body);
    }

    public static Constant parseConstant(ParserContext context, Token name) throws SyntaxException
    {
        // TODO: check scope for duplicates

        Type type = Parser.parseType(context);

        context.expect(TokenType.ASSIGN);

        Expression value = Expression.parse(context);

        context.expect(TokenType.TERMINATOR);

        return new Constant(name, type, value);
    }

    public static Import parseImport(ParserContext context) throws SyntaxException
    {
        Token path = context.expect(TokenType.LITERAL);
        return new Import(path);
    }

    private static Return parseReturn(ParserContext context) throws SyntaxException
    {
        // TODO: multi returns
        Expression value = Expression.parse(context);
        return new Return(value);
    }

    private static Statement simple(ParserContext context, Token name) throws SyntaxException
    {
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
                return new Assignment(name, value);
            case DECLARE:
                return parseVariableDeclaration(context, name);
            case DEFINE:
                return parseVariableDefine(context, name);
            default:
                return null;
        }
    }

    private static Body parseBody(ParserContext context) throws SyntaxException
    {
        context.expect(TokenType.LEFT_BRACE);

        Scope parent = context.getCurrentScope();

        Body body = new Body(parent.copy());

        context.setCurrentScope(body.getScope());

        while (!context.has(TokenType.RIGHT_BRACE))
        {
            body.getStatements().add(Statement.parse(context));
        }

        context.setCurrentScope(parent);

        return body;
    }

    private static boolean symbolExists(ParserContext context, Token name)
    {
        return getPreviousDeclaration(context, name) != null;
    }

    private static Declaration getPreviousDeclaration(ParserContext context, Token name)
    {
        Scope scope = context.getCurrentScope();
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

    public static class Assignment extends Statement
    {
        private Token name;
        private Expression expression;

        private Assignment(Token name, Expression expression)
        {
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
    }

    public static class Constant extends Statement implements Declaration
    {
        private final Token name;
        private final Type type;
        private final Expression value;

        private Constant(Token name, Type type, Expression value)
        {
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
    }

    public static class Return extends Statement
    {
        private Expression expression;

        private Return(Expression expression)
        {
            this.expression = expression;
        }

        public Expression getExpression()
        {
            return expression;
        }
    }

    public static class Import extends Statement implements Declaration
    {
        private final Token path;

        private Import(Token path)
        {
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

        private For(Body body)
        {
            this.body = body;
        }

        private For(Expression condition, Body body)
        {
            this.condition = condition;
            this.body = body;
        }

        protected For(Assignment index, Expression condition, Statement counter, Body body)
        {
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
    }

    public static class If extends Statement
    {
        private Expression condition;
        private Body body;

        private If(Expression condition, Body body)
        {
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
    }

    public static class VariableDeclaration extends Statement implements Declaration
    {
        private final Variable variable;
        private final Expression expression;

        private VariableDeclaration(Token name, Type type)
        {
            this(name, type, null);
        }

        private VariableDeclaration(Token name, Type type, Expression expression)
        {
            this.variable = new Variable(name, type);
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
    }
}
