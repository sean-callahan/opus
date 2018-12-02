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
    private static final int TERMINATOR = ';';

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
        boolean eof;
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
            case TERMINATOR:
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
            case '%':
                return emit(TokenType.OPERATOR, Operator.MOD);
            case ':':
                rune = input.read();
                if (rune == ':')
                {
                    return emit(TokenType.DECLARE_GLOBAL);
                }
                else if (rune == '=')
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
                rune = input.read();
                if (rune == '+')
                {
                    return emit(TokenType.OPERATOR_DOUBLE, Operator.ADD);
                }
                else if (rune == '=')
                {
                    return emit(TokenType.OPERATOR_ASSIGN, Operator.ADD);
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
                    return emit(TokenType.OPERATOR_DOUBLE, Operator.SUBTRACT);
                }
                else if (rune == '=')
                {
                    return emit(TokenType.OPERATOR_ASSIGN, Operator.SUBTRACT);
                }
                else if (rune >= '0' && rune <= '9')
                {
                    input.unread(rune);
                    return number('-');
                }
                input.unread(rune);
                return emit(TokenType.OPERATOR, Operator.SUBTRACT);
            case '/':
                rune = input.read();
                if (rune == '/')
                {
                    return comment();
                }
                else if (rune == '=')
                {
                    return emit(TokenType.OPERATOR_ASSIGN, Operator.DIVIDE);
                }
                else if (rune == '*')
                {
                    return commentMulti();
                }
                input.unread(rune);
                return emit(TokenType.OPERATOR, Operator.DIVIDE);
            case '*':
                rune = input.read();
                if (rune == '=')
                {
                    return emit(TokenType.OPERATOR_ASSIGN, Operator.MULTIPLY);
                }
                input.unread(rune);
                return emit(TokenType.OPERATOR, Operator.MULTIPLY);
            case '<':
                rune = input.read();
                if (rune == '=')
                {
                    return emit(TokenType.OPERATOR, Operator.LEQ);
                }
                else if (rune == '<')
                {
                    return emit(TokenType.OPERATOR, Operator.LSHIFT);
                }
                input.unread(rune);
                return emit(TokenType.OPERATOR, Operator.LT);
            case '>':
                rune = input.read();
                if (rune == '=')
                {
                    return emit(TokenType.OPERATOR, Operator.GEQ);
                }
                else if (rune == '>')
                {
                    return emit(TokenType.OPERATOR, Operator.RSHIFT);
                }
                input.unread(rune);
                return emit(TokenType.OPERATOR, Operator.GT);
            case '|':
                rune = input.read();
                if (rune != '|')
                {
                    throw new Error(String.format("invalid character '%c'", (char)rune), position);
                }
                return emit(TokenType.OPERATOR, Operator.OR);
            case '&':
                rune = input.read();
                if (rune != '&')
                {
                    throw new Error(String.format("invalid character '%c'", (char)rune), position);
                }
                return emit(TokenType.OPERATOR, Operator.AND);
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

        String keyword = buffer.toString();

        if (keywords.containsKey(keyword))
        {
            return emit(keywords.get(keyword));
        }

        return emit(TokenType.NAME);
    }

    private boolean number(int first) throws IOException
    {
        buffer.append((char)first);

        int rune = input.read();
        while (('0' <= rune && rune <= '9') || rune == '.') {
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

    private boolean commentMulti() throws IOException
    {
        int rune = 0;
        int last;
        do
        {
            last = rune;
            rune = input.read();
        } while (rune != EOF && !(last == '*' && rune == '/'));
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
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("if", TokenType.IF);
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

