package net.seancallahan.opus.compiler;

public class CompilerException extends Exception
{
    private final String message;
    private final SourceFile.Position position;

    public CompilerException(String message)
    {
        this(message, null);
    }

    public CompilerException(String message, SourceFile.Position position)
    {
        this.message = message;
        this.position = position;
    }

    @Override
    public String getMessage()
    {
        if (position == null)
        {
            return message;
        }
        return String.format("%s at line %d, column %d in %s", message,
                position.getLine(), position.getColumn(), position.getFile().getPath());
    }

    public SourceFile.Position getPosition()
    {
        return position;
    }
}
