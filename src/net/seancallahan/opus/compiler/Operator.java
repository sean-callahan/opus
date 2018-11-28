package net.seancallahan.opus.compiler;

import java.util.HashMap;

public enum Operator
{
    NONE,
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,

    NOT,
    NEQ,
    EQ,
    LT,
    LEQ,
    GT,
    GEQ;

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

        text.put(NOT, "!");
        text.put(NEQ, "!=");
        text.put(EQ, "==");
        text.put(LT, "<");
        text.put(LEQ, "<=");
        text.put(GT, ">");
        text.put(GEQ, ">=");
    }
}
