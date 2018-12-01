package net.seancallahan.opus.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Lexer
{
    private static final int EOF = -1;

    private final PushbackInputStream input;

    private final ArrayList<Token> tokens = new ArrayList<>();

    private StringBuilder buffer = new StringBuilder();

    private final SourceFile.Position position;

    public Lexer(File file) throws IOException, Error
    {
        this.input = new PushbackInputStream(new FileInputStream(file));
        this.position = new SourceFile.Position(file, 1, 0);

        lex();
    }

    private void lex() throws IOException, Error
    {
        boolean eof = false;
        do
        {
            eof = next();
        } while (!eof);
    }

    private boolean next() throws IOException, Error
    {
        int rune = input.read();

        position.incrementColumn();

        if (rune == EOF)
        {
            return emit(TokenType.EOF);
        }

        if (rune == '\n')
        {
            position.incrementLine();
            position.setColumn(0);
            return false;
        }

        if (Character.isWhitespace(rune))
        {
            return false;
        }

        if (Character.isLetter(rune))
        {
            return identifier(rune);
        }

        if ('0' <= rune && rune <= '9')
        {
            return number(rune);
        }

        switch (rune)
        {
            case ';':
                return emit(TokenType.TERMINATOR);
            case '{':
                return emit(TokenType.LEFT_BRACE);
            case '[':
                return emit(TokenType.LEFT_BRACKET);
            case '(':
                return emit(TokenType.LEFT_PAREN);
            case '}':
                return emit(TokenType.RIGHT_BRACE);
            case ']':
                return emit(TokenType.RIGHT_BRACKET);
            case ')':
                return emit(TokenType.RIGHT_PAREN);
            case ',':
                return emit(TokenType.COMMA);
            case '.':
                return emit(TokenType.DOT);
            case '"':
                return string();
            case ':':
                if ((rune = input.read()) == ':')
                {
                    return emit(TokenType.DECLARE_GLOBAL);
                }
                input.unread(rune);
                if ((rune = input.read()) == '=')
                {
                    return emit(TokenType.DEFINE);
                }
                input.unread(rune);
                return emit(TokenType.DECLARE);
            case '=':
                if ((rune = input.read()) == '=')
                {
                    return emit(TokenType.OPERATOR, Operator.EQ);
                }
                input.unread(rune);
                return emit(TokenType.ASSIGN);
            case '+':
                if ((rune = input.read()) == '+')
                {
                    return emit(TokenType.OPERATOR, Operator.INCR);
                }
                input.unread(rune);
                return emit(TokenType.OPERATOR, Operator.ADD);
            case '-':
                rune = input.read();
                if (rune == '>')
                {
                    return emit(TokenType.RETURNS);
                }
                else if (rune == '-')
                {
                    return emit(TokenType.OPERATOR, Operator.DECR);
                }
                input.unread(rune);
                return emit(TokenType.OPERATOR, Operator.SUBTRACT);
            case '/':
                if ((rune = input.read()) == '/')
                {
                    return comment();
                }
                input.unread(rune);
                return emit(TokenType.OPERATOR, Operator.DIVIDE);
            case '*':
                return emit(TokenType.OPERATOR, Operator.MULTIPLY);
            case '<':
                if ((rune = input.read()) == '=')
                {
                    return emit(TokenType.OPERATOR, Operator.LEQ);
                }
                input.unread(rune);
                return emit(TokenType.OPERATOR, Operator.LT);
            case '>':
                if ((rune = input.read()) == '=')
                {
                    return emit(TokenType.OPERATOR, Operator.GEQ);
                }
                input.unread(rune);
                return emit(TokenType.OPERATOR, Operator.GT);
            default:
                throw new Error(String.format("invalid character '%c'", (char)rune), position);
        }
    }

    public List<Token> getTokens()
    {
        return tokens;
    }

    private boolean emit(TokenType type)
    {
        emit(type, Operator.NONE);

        return type == TokenType.EOF;
    }

    private boolean emit(TokenType type, Operator operator)
    {
        tokens.add(new Token(type, buffer.toString(), SourceFile.Position.copy(this.position), operator));

        reset();

        return type == TokenType.EOF;
    }

    private void reset()
    {
        if (buffer.length() > 0)
        {
            buffer = new StringBuilder();
        }
    }

    private boolean identifier(int first) throws IOException
    {
        buffer.append((char)first);

        int rune = input.read();

        while (Character.isLetter(rune) || Character.isDigit(rune))
        {
            buffer.append((char)rune);
            rune = input.read();
        }

        input.unread(rune);

        position.setColumn(position.getColumn() + buffer.length());

        for (String keyword : keywords.keySet())
        {
            if (buffer.toString().equals(keyword))
            {
                reset();
                emit(keywords.get(keyword));
                return false;
            }
        }

        return emit(TokenType.NAME);
    }

    private boolean number(int first) throws IOException
    {
        buffer.append((char)first);

        int rune = input.read();
        while ('0' <= rune && rune <= '9') {
            buffer.append((char)rune);
            rune = input.read();
        }

        input.unread(rune);

        return emit(TokenType.LITERAL);
    }

    private boolean comment() throws IOException
    {
        int rune = input.read();
        while (rune != '\n')
        {
            rune = input.read();
        }
        return false;
    }

    private boolean string() throws IOException
    {
        int rune = input.read();
        while (rune != '"') {
            buffer.append((char)rune);
            rune = input.read();
        }

        return emit(TokenType.LITERAL);
    }

    private static final HashMap<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("const", TokenType.CONST);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("if", TokenType.IF);
        keywords.put("in", TokenType.IN);
        keywords.put("import", TokenType.IMPORT);
        keywords.put("nil", TokenType.NIL);
        keywords.put("package", TokenType.PACKAGE);
        keywords.put("return", TokenType.RETURN);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
    }

    public static class Error extends CompilerException
    {
        public Error(String message, SourceFile.Position position)
        {
            super(message, position);
        }
    }
}

