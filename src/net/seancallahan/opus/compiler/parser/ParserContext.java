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
    private final PeekableIterator<Token> iterator;

    public ParserContext(SourceFile file, List<Token> tokens)
    {
        this.source = file;
        this.iterator = new PeekableIterator<>(tokens);
    }

    public SourceFile getSource()
    {
        return source;
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

}
