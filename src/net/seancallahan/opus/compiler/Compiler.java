package net.seancallahan.opus.compiler;

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

    public SourceFile compile(File file, boolean assemble) throws Exception
    {
        SourceFile sourceFile = new SourceFile(file);
        sourceFile.lex();
        sourceFile.parse();
        if (assemble)
        {
            sourceFile.compile();
        }

        return sourceFile;
    }

}
