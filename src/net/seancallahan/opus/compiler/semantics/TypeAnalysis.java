package net.seancallahan.opus.compiler.semantics;

import net.seancallahan.opus.compiler.CompilerException;
import net.seancallahan.opus.compiler.parser.Parser;
import net.seancallahan.opus.lang.Class;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Method;

public class TypeAnalysis
{
    public TypeAnalysis(Parser parser) throws CompilerException
    {
        for (Declaration decl : parser.getDeclarations().values())
        {
            if (decl instanceof Method)
            {
                Method m = (Method) decl;
                if (m.getParent() == null)
                {
                    Declaration d = parser.getDeclarations().get(m.getParentName());
                    if (!(d instanceof Class))
                    {
                        throw new CompilerException(String.format("%s is not a class", d.getName()));
                    }
                    Class clazz = (Class)d;
                    m.setParent(clazz);
                    clazz.getMethods().add(m);
                }
            }
        }
    }
}
