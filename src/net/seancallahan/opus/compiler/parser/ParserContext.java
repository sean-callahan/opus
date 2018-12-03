package net.seancallahan.opus.compiler.parser;

import net.seancallahan.opus.compiler.Operator;
import net.seancallahan.opus.compiler.SourceFile;
import net.seancallahan.opus.compiler.Token;
import net.seancallahan.opus.compiler.TokenType;
import net.seancallahan.opus.util.PeekableIterator;

import java.util.List;

public class ParserContext
{
    private final SourceFile source;
    private final Parser parser;
    private final PeekableIterator<Token> iterator;

    private State state = State.NONE;
    private Body currentBody;

    public ParserContext(SourceFile file, Parser parser, List<Token> tokens)
    {
        this.source = file;
        this.parser = parser;
        this.iterator = new PeekableIterator<>(tokens);
    }

    public SourceFile getSource()
    {
        return source;
    }

    public Parser getParser()
    {
        return parser;
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public Body getCurrentBody()
    {
        return currentBody;
    }

    public void setCurrentBody(Body currentBody)
    {
        this.currentBody = currentBody;
    }

    public PeekableIterator<Token> getIterator()
    {
        return iterator;
    }

    public Token expect(TokenType type) throws SyntaxException
    {
        Token token = getIterator().next();
        if (token.getType() != type)
        {
            throw new SyntaxException(String.format("unexpected %s, expecting %s", token.getType(), type), token);
        }
        return token;
    }

    public boolean matches(Operator... operators)
    {
        Token next = getIterator().peek();

        if (next.getType() != TokenType.OPERATOR)
        {
            return false;
        }

        for (Operator operator : operators)
        {
            if (next.getOperator() == operator)
            {
                return true;
            }
        }

        return false;
    }

    public boolean matches(TokenType... types)
    {
        Token token = getIterator().peek();

        for (TokenType type : types)
        {
            if (token.getType() == type)
            {
                return true;
            }
        }

        return false;
    }

    public boolean has(TokenType type)
    {
        Token next = getIterator().peek();
        if (next.getType() != type)
        {
            return false;
        }
        getIterator().skip(1);
        return true;
    }

    public enum State
    {
        NONE,
        CLASS,
        FUNCTION,
    }

}
