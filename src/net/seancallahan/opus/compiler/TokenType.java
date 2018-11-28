package net.seancallahan.opus.compiler;

import java.util.HashMap;

public enum TokenType
{
    EOF,
    TERMINATOR,

    NAME,
    LITERAL,

    OPERATOR,
    ASSIGN,
    DEFINE,
    RETURNS,
    DECLARE,
    SET,

    LEFT_BRACE,
    LEFT_BRACKET,
    LEFT_PAREN,
    RIGHT_BRACE,
    RIGHT_BRACKET,
    RIGHT_PAREN,
    COMMA,
    DOT,

    CONST,
    ELSE,
    FALSE,
    FOR,
    IF,
    IN,
    IMPORT,
    NIL,
    PACKAGE,
    RETURN,
    THIS,
    TRUE,
    VAR;

    @Override
    public String toString()
    {
        if (text.containsKey(this))
        {
            return text.get(this);
        }
        return super.toString();
    }

    private static final HashMap<TokenType, String> text = new HashMap<>();

    static
    {
        text.put(TERMINATOR, "semicolon");
        text.put(DECLARE, "::");
        text.put(RETURNS, "->");

        text.put(LEFT_BRACE, "{");
        text.put(LEFT_BRACKET, "[");
        text.put(LEFT_PAREN, "(");
        text.put(RIGHT_BRACE, "}");
        text.put(RIGHT_BRACKET, "]");
        text.put(RIGHT_PAREN, ")");

        text.put(CONST, "const");
        text.put(ELSE, "else");
        text.put(FOR, "for");
        text.put(IF, "if");
        text.put(IN, "in");
        text.put(IMPORT, "import");
        text.put(PACKAGE, "package");
        text.put(RETURN, "return");
        text.put(THIS, "this");
        text.put(VAR, "var");
    }
}
