package net.seancallahan.opus.compiler;

import net.seancallahan.opus.compiler.jvm.ClassFile;
import net.seancallahan.opus.compiler.parser.Parser;
import net.seancallahan.opus.compiler.semantics.ReferenceResolver;
import net.seancallahan.opus.compiler.semantics.TypeAnalysis;
import net.seancallahan.opus.lang.Class;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Method;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SourceFile
{
    private final File file;

    private List<Token> tokens;
    private Parser parser;

    public SourceFile(File file)
    {
        this.file = file;
    }

    public Parser getParser()
    {
        return parser;
    }

    public void lex() throws IOException, Lexer.Error
    {
        this.tokens = new Lexer(file).getTokens();
    }

    public void parse() throws CompilerException
    {
        if (tokens == null)
        {
            throw new IllegalStateException("must first call lex()");
        }

        this.parser = new Parser(this, tokens, new Scope(null));
        this.parser.parse();

        ReferenceResolver referenceResolver = new ReferenceResolver(parser);
        referenceResolver.resolve();

        TypeAnalysis typeAnalysis = new TypeAnalysis(parser);
        typeAnalysis.perform();
    }

    public void compile() throws IOException, CompilerException
    {
        String basePath = file.getParentFile().getAbsolutePath();

        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }

        List<Function> staticFunctions = new ArrayList<>();

        for (Declaration declaration : parser.getDeclarations().values())
        {
            if (declaration instanceof Class)
            {
                Class clazz = (Class)declaration;
                String name = clazz.getName().getValue();
                createClassFile(basePath, fileName + "_" + name, new ClassFile(file, clazz));
            }
            else if (declaration instanceof Function && !(declaration instanceof Method))
            {
                staticFunctions.add((Function) declaration);
            }
        }

        if (staticFunctions.size() > 0)
        {
            createClassFile(basePath, fileName + "_static", new ClassFile(file, fileName + "_static", "test", staticFunctions));
        }
    }

    private static void createClassFile(String base, String name, ClassFile file) throws IOException, CompilerException
    {
        FileOutputStream s = new FileOutputStream(base + File.pathSeparator + name + ".class");
        file.write(new DataOutputStream(s));
        s.close();
    }

    public static class Position
    {
        private final File file;

        private int line;
        private int column;

        public Position(File file, int line, int column)
        {
            this.file = file;
            this.line = line;
            this.column = column;
        }

        public File getFile()
        {
            return file;
        }

        public int getLine()
        {
            return line;
        }

        public void setLine(int line)
        {
            this.line = line;
        }

        public int getColumn()
        {
            return column;
        }

        public void setColumn(int column)
        {
            this.column = column;
        }

        public void incrementColumn()
        {
            this.column++;
        }

        public void incrementLine()
        {
            this.line++;
        }

        public static SourceFile.Position copy(SourceFile.Position other)
        {
            return new SourceFile.Position(other.file, other.line, other.column);
        }
    }

}
