package net.seancallahan.opus.compiler.semantics;

import net.seancallahan.opus.compiler.Scope;
import net.seancallahan.opus.compiler.Token;
import net.seancallahan.opus.compiler.parser.Expression;
import net.seancallahan.opus.compiler.parser.SyntaxException;

public interface Resolver
{
    Object resolve(Expression expression) throws SyntaxException;
    Object resolve(Scope scope, Token token) throws SyntaxException;
}
