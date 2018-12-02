package net.seancallahan.opus.compiler;

import java.util.HashMap;

public enum Operator
{
    NONE,
    ADD,      // +
    SUBTRACT, // -
    MULTIPLY, // *
    DIVIDE,   // /

    MOD,      // %
    LSHIFT,   // <<
    RSHIFT,   // >>

    OR,       // ||
    AND,      // &&
    NOT,      // !
    EQ,       // ==
    LT,       // <
    GT,       // >

    NEQ,      // !=
    LEQ,      // <=
    GEQ,      // >=
    ;

    @Override
    public String toString()
    {
        if (text.containsKey(this))
        {
            return text.get(this);
        }
        return super.toString();
    }

    private static final HashMap<Operator, String> text = new HashMap<>();

    static
    {
        text.put(ADD, "+");
        text.put(SUBTRACT, "-");
        text.put(MULTIPLY, "*");
        text.put(DIVIDE, "/");

        text.put(MOD, "%");
        text.put(LSHIFT, "<<");
        text.put(RSHIFT, ">>");

        text.put(OR, "||");
        text.put(AND, "&&");
        text.put(NOT, "!");
        text.put(EQ, "==");
        text.put(LT, "<");
        text.put(GT, ">");

        text.put(NEQ, "!=");
        text.put(LEQ, "<=");
        text.put(GEQ, ">=");
    }
}
