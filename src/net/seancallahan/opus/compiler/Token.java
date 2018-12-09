package net.seancallahan.opus.compiler;

import java.io.DataOutputStream;
import java.io.IOException;

public class Token
{
    private final TokenType type;

    private final String value;

    private final SourceFile.Position position;

    private final Operator operator;

    public Token(TokenType type, String value)
    {
        this.type = type;
        this.value = value;
        this.position = null;
        this.operator = null;
    }

    public Token(TokenType type, String value, SourceFile.Position position, Operator operator)
    {
        this.type = type;
        this.value = value;
        this.position = position;
        this.operator = operator;
    }

    public TokenType getType()
    {
        return type;
    }

    public String getValue()
    {
        return value;
    }

    public SourceFile.Position getPosition()
    {
        return position;
    }

    public Operator getOperator()
    {
        return operator;
    }

    @Override
    public String toString()
    {
        String out = getType().toString();
        if (getType() == TokenType.OPERATOR)
        {
            out += "=" + getOperator();
        }
        else if (getType() == TokenType.NAME || getType() == TokenType.LITERAL)
        {
            out += "=" + getValue();
        }
        return out;
    }

    public void writeTo(DataOutputStream out) throws IOException
    {
        out.writeByte(type.ordinal());
        out.writeUTF(value);
    }

}
