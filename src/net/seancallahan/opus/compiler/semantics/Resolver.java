package net.seancallahan.opus.compiler.semantics;

import net.seancallahan.opus.compiler.Scope;
import net.seancallahan.opus.compiler.Token;
import net.seancallahan.opus.compiler.parser.Expression;
import net.seancallahan.opus.compiler.parser.SyntaxException;

public interface Resolver
{
    void resolve(Scope scope, Expression expression) throws SyntaxException;
    void resolve(Scope scope, Token token) throws SyntaxException;
}
