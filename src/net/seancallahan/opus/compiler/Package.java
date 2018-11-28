package net.seancallahan.opus.compiler;

import java.util.ArrayList;
import java.util.List;

public class Package
{
    private final String name;

    private final List<SourceFile> files = new ArrayList<>();
    private final Scope scope = new Scope(null);

    public Package(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public List<SourceFile> getFiles()
    {
        return files;
    }

    public Scope getScope()
    {
        return scope;
    }
}
