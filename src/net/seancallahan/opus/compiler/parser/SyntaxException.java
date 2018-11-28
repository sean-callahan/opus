package net.seancallahan.opus.compiler.parser;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.Token;

public class SyntaxException extends CompilerException
{
    public SyntaxException(String message)
    {
        super(message);
    }

    public SyntaxException(String message, Token at)
    {
        super(message, at.getPosition());
    }
}
