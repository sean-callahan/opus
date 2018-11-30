package net.seancallahan.opus.compiler;

import net.seancallahan.opus.compiler.semantics.TypeAnalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Compiler
{
    private final List<Package> packages = new ArrayList<>();

    public Compiler()
    {

    }

    public List<Package> getPackages()
    {
        return packages;
    }

    public void compile(File file) throws Exception
    {
        SourceFile sourceFile = new SourceFile(file);
        sourceFile.lex();
        sourceFile.parse();
        sourceFile.compile();
    }


}
