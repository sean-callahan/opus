package net.seancallahan.opus.compiler.jvm;

import net.seancallahan.opus.lang.Type;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.compiler.Function;
import net.seancallahan.opus.lang.Method;
import net.seancallahan.opus.lang.Variable;

public class Descriptor
{
    private final Object of;
    private final String value;

    private Descriptor(Object of, String value)
    {
        this.of = of;
        this.value = value;
    }

    public Object getOf()
    {
        return of;
    }

    @Override
    public String toString()
    {
        return value;
    }

    public static Descriptor from(Declaration decl)
    {
        if (decl.getClass().isAssignableFrom(Variable.class))
            return from((Variable) decl);
        else if (decl instanceof Method || decl instanceof Function)
            return from((Function) decl);
        throw new UnsupportedOperationException("Cannot format declaration of type: " + decl.getClass());
    }

    public static Descriptor from(Variable field)
    {
        return new Descriptor(field, findTerm(field.getType().getName()));
    }

    public static Descriptor from(Function function)
    {
        StringBuilder b = new StringBuilder();

        b.append('(');

        for (Variable param : function.getParameters())
        {
            b.append(findTerm(param.getType().getName()));
        }

        b.append(')');

        if (function.getReturns().size() > 1)
        {
            // TODO: create tuple to hold multi returns
            throw new UnsupportedOperationException();
        }

        if (function.getReturns().size() == 1)
        {
            Type returns = function.getReturns().get(0).getType();
            b.append(findTerm(returns.getName()));
        }
        else
        {
            b.append('V');
        }

        return new Descriptor(function, b.toString());
    }

    private static String findTerm(String type)
    {
        boolean array = type.startsWith("[]");
        if (array)
        {
            return "[" + findTerm(type.substring(2));
        }

        switch (type)
        {
            case "bool":
                return "Z";
            case "u8":
            case "s8":
                return "B";
            case "u16":
            case "s16":
                return "S";
            case "u32":
            case "s32":
                return "I";
            case "u64":
            case "s64":
                return "J";
            case "f32":
                return "F";
            case "f64":
                return "D";
            default:
                // assume reference
                return "L" + type + ";";
        }
    }
}
