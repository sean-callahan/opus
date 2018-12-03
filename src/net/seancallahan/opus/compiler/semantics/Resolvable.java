package net.seancallahan.opus.compiler.semantics;

import net.seancallahan.opus.compiler.parser.SyntaxException;

public interface Resolvable
{
    void resolve(Resolver resolver) throws SyntaxException;
}
