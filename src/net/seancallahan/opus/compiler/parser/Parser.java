package net.seancallahan.opus.compiler.parser;

import net.seancallahan.opus.compiler.*;
import net.seancallahan.opus.lang.*;
import net.seancallahan.opus.lang.Class;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser
{
    private final ParserContext context;

    private final Map<String, Declaration> declarations = new HashMap<>();

    private String packageName;

    private final Scope global;

    public Parser(SourceFile file, List<Token> tokens, Scope global)
    {
        this.context = new ParserContext(file, this, tokens);
        this.global = global;
    }

    public Scope getGlobalScope()
    {
        return global;
    }

    public void parse() throws CompilerException
    {
        context.expect(TokenType.PACKAGE);

        packageName = context.expect(TokenType.NAME).getValue();

        context.expect(TokenType.TERMINATOR);

        while (context.getIterator().hasNext())
        {
            Token next = context.getIterator().next();

            switch (next.getType())
            {
                case CONST:
                case IMPORT:
                    declaration(next);
                    break;
                case NAME:
                    name(next);
                    break;
                case EOF:
                    return;
            }
        }
    }

    public String getPackageName()
    {
        return packageName;
    }

    private void addDeclaration(Declaration declaration) throws CompilerException
    {
        Token name = declaration.getName();

        if (declarations.containsKey(name.getValue()))
        {
            throw new CompilerException(String.format("Symbol %s already declared.", name));
        }

        declarations.put(name.getValue(), declaration);
        global.add(declaration);
    }

    private void declaration(Token keyword) throws CompilerException
    {
        Declaration declaration;

        switch (keyword.getType())
        {
            case CONST:
                declaration = Statement.parseConstant(context);
                break;
            case IMPORT:
                declaration = Statement.parseImport(context);
                break;
            default:
                throw new SyntaxException("invalid keyword");
        }

        addDeclaration(declaration);
    }

    private void name(Token name) throws CompilerException
    {
        if (context.has(TokenType.DECLARE_GLOBAL))
        {
            declare(name);
        }
        else if (context.has(TokenType.DOT))
        {
            method(name);
        }
        else
        {
            throw new SyntaxException("invalid character after name");
        }
    }

    private void declare(Token name) throws CompilerException
    {
        Declaration declaration;

        Token next = context.getIterator().peek();
        switch (next.getType())
        {
            case LEFT_PAREN:
                Function func = new Function(name, Scope.childOf(global));
                parseFunction(context, func);
                declaration = func;
                break;
            case LEFT_BRACE:
                Class clazz = new Class(name);
                parseClass(context, clazz);
                declaration = clazz;
                break;
            default:
                throw new SyntaxException(String.format("expected %s or %s got %s",
                    TokenType.LEFT_PAREN, TokenType.LEFT_BRACE, next.getType()));
        }

        addDeclaration(declaration);
    }

    private void method(Token parent) throws CompilerException
    {
        Token name = context.expect(TokenType.NAME);

        context.expect(TokenType.DECLARE_GLOBAL);

        // TODO: scope _should_ be child of parent class'.
        Method method = new Method(parent, name, Scope.childOf(global));
        parseFunction(context, method);

        addDeclaration(method);
    }

    private static void parseFunction(ParserContext context, Function func) throws CompilerException {
        context.setCurrentFunction(func);

        parseSignature(context, func.getParameters(), func.getReturns());

        context.expect(TokenType.LEFT_BRACE);

        parseBody(context, func.getBody());

        context.expect(TokenType.RIGHT_BRACE);

        context.setCurrentFunction(null);
    }

    private static void parseSignature(ParserContext context, List<Variable> params, List<Variable> returns) throws SyntaxException
    {
        context.expect(TokenType.LEFT_PAREN);

        parseVariableList(context, params);

        context.expect(TokenType.RIGHT_PAREN);

        if (context.has(TokenType.RETURNS))
        {
            context.expect(TokenType.LEFT_PAREN);
            parseVariableList(context, returns);
            context.expect(TokenType.RIGHT_PAREN);
        }
    }

    private static void parseVariableList(ParserContext context, Collection<Variable> list) throws SyntaxException
    {
        list.clear();

        if (context.getIterator().peek().getType() != TokenType.NAME)
        {
            return;
        }

        while (context.getIterator().hasNext())
        {
            Token name = context.expect(TokenType.NAME);

            Type type = parseType(context);

            list.add(new Variable(name, type));

            if (context.getIterator().peek().getType() != TokenType.COMMA)
            {
                break;
            }

            context.getIterator().skip(1);
        }
    }

    private static void parseBody(ParserContext context, Body body) throws SyntaxException
    {
        while (context.getIterator().peek().getType() != TokenType.RIGHT_BRACE)
        {
            body.getStatements().add(Statement.parse(context));
        }
    }

    private static void parseClass(ParserContext context, Class clazz) throws SyntaxException
    {
        context.expect(TokenType.LEFT_BRACE);

        parseFields(context, clazz.getFields());

        context.expect(TokenType.RIGHT_BRACE);
    }

    private static void parseFields(ParserContext context, List<Variable> fields) throws SyntaxException
    {
        while (context.getIterator().peek().getType() != TokenType.RIGHT_BRACE)
        {
            Token name = context.getIterator().next();
            if (name.getType() != TokenType.NAME)
            {
                throw new SyntaxException("expecting field name", name);
            }

            Type type = parseType(context);

            fields.add(new Variable(name, type));

            context.expect(TokenType.TERMINATOR);
        }
    }

    public static Type parseType(ParserContext context) throws SyntaxException
    {
        Token next = context.getIterator().next();
        switch (next.getType())
        {
            case NAME:
                return new Type(next);
            case LEFT_BRACKET:
                context.expect(TokenType.RIGHT_BRACKET);
                Token name = context.expect(TokenType.NAME);
                // TODO: fix arrays
                return new Type(null);
            default:
                throw new SyntaxException(String.format("expecting type got %s", next), next);
        }
    }

    public Map<String, Declaration> getDeclarations()
    {
        return declarations;
    }
}
