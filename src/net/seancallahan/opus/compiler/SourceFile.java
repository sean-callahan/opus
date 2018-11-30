package net.seancallahan.opus.compiler;

import net.seancallahan.opus.compiler.jvm.ClassFile;
import net.seancallahan.opus.compiler.parser.Parser;
import net.seancallahan.opus.compiler.semantics.TypeAnalysis;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Class;

import java.io.*;
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

        new TypeAnalysis(parser);
    }

    public void compile() throws IOException, CompilerException
    {
        if (parser == null)
        {
            throw new IllegalStateException("must first call parse()");
        }

        for (Declaration decl : parser.getDeclarations().values())
        {
            if (decl instanceof Class)
            {
                ClassFile cf = new ClassFile((Class) decl);
                FileOutputStream s = new FileOutputStream("/Users/sean/Desktop/class_" + decl.getName() + ".class");
                cf.write(new DataOutputStream(s));
                s.close();
            }
        }
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
