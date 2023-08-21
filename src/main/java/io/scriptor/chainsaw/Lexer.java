package io.scriptor.chainsaw;

import java.util.List;
import java.util.Vector;

public class Lexer {

    private final StringEater mEater;

    public Lexer(StringEater eater) {
        this.mEater = eater;
    }

    public List<Token> tokenize() {
        mEater.reset();

        List<Token> tokens = new Vector<>();
        int line = 1;

        while (mEater.ok()) {
            char c = mEater.eat();

            // unused
            if (isIgnorable(c)) {
                if (c == '\n')
                    line++;
                continue;
            }

            // comment
            if (c == '#') {
                // single line
                if (mEater.next() == '#') {
                    while (mEater.eat() != '\n')
                        ;
                    line++;
                    continue;
                }

                // multi
                while ((c = mEater.eat()) != '#')
                    if (c == '\n')
                        line++;
                continue;
            }

            // identifier
            if (isAlpha(c)) {
                StringBuilder ident = new StringBuilder().append(c);

                while (isAlnum(mEater.next()))
                    ident.append(mEater.eat());

                tokens.add(new Token()
                        .type(TokenType.IDENTIFIER)
                        .value(ident.toString())
                        .line(line));

                continue;
            }

            // number
            if (isDigit(c) || (c == '.' && isDigit(mEater.next()))) {
                StringBuilder num = new StringBuilder().append(c);
                TokenType type = c == '.' ? TokenType.NUMBER_FLOAT : TokenType.NUMBER_INT;

                while (isDigit(mEater.next()) || mEater.next() == '.') {
                    c = mEater.eat();
                    if (c == '.') {
                        if (type == TokenType.NUMBER_FLOAT)
                            error(line, "too many period chars in number");
                        type = TokenType.NUMBER_FLOAT;
                    }

                    num.append(c);
                }

                tokens.add(new Token()
                        .type(type)
                        .value(num.toString())
                        .line(line));

                continue;
            }

            if (c == '"') {
                StringBuilder string = new StringBuilder();
                int startLine = line;

                while (mEater.next() != '"') {
                    if (mEater.next() == '\n')
                        line++;
                    string.append(mEater.eat());
                }

                mEater.eat();

                tokens.add(new Token()
                        .type(TokenType.STRING)
                        .value(string.toString())
                        .line(startLine));

                continue;
            }

            if (c == '\'') {
                StringBuilder character = new StringBuilder();

                while (mEater.next() != '\'')
                    character.append(mEater.eat());

                mEater.eat();

                tokens.add(new Token()
                        .type(TokenType.CHAR)
                        .value(character.toString())
                        .line(line));

                continue;
            }

            TokenType type = TokenType.UNDEFINED;
            switch (c) {

                case '(':
                    type = TokenType.PAREN_OPEN;
                    break;

                case ')':
                    type = TokenType.PAREN_CLOSE;
                    break;

                case '{':
                    type = TokenType.BRACE_OPEN;
                    break;

                case '}':
                    type = TokenType.BRACE_CLOSE;
                    break;

                case '[':
                    type = TokenType.BRACKET_OPEN;
                    break;

                case ']':
                    type = TokenType.BRACKET_CLOSE;
                    break;

                case '.':
                    type = TokenType.PERIOD;
                    break;

                case ':':
                    type = TokenType.COLON;
                    break;

                case ',':
                    type = TokenType.COMMA;
                    break;

                case ';':
                    type = TokenType.SEMICOLON;
                    break;

                case '+':
                    type = TokenType.PLUS;
                    break;

                case '-':
                    type = TokenType.MINUS;
                    break;

                case '*':
                    type = TokenType.ASTER;
                    break;

                case '/':
                    type = TokenType.SLASH;
                    break;

                case '^':
                    type = TokenType.CARET;
                    break;

                case '!':
                    type = TokenType.EXCLAM;
                    break;

                case '§':
                    type = TokenType.PARAGR;
                    break;

                case '$':
                    type = TokenType.DOLLAR;
                    break;

                case '%':
                    type = TokenType.PERCENT;
                    break;

                case '&':
                    type = TokenType.AND;
                    break;

                case '=':
                    type = TokenType.EQUAL;
                    break;

                case '?':
                    type = TokenType.QUEST;
                    break;

                case '~':
                    type = TokenType.TILDE;
                    break;

                case '<':
                    type = TokenType.LESS;
                    break;

                case '>':
                    type = TokenType.GREATER;
                    break;

                case '|':
                    type = TokenType.PIPE;
                    break;

                default:
                    error(line, "undefined char '%s'", c);
                    break;
            }

            tokens.add(new Token()
                    .type(type)
                    .value(c)
                    .line(line));
        }

        return tokens;
    }

    private static boolean isIgnorable(char c) {
        return c <= 0x20;
    }

    private static boolean isAlpha(char c) {
        return (c >= 0x41 && c <= 0x5A) || (c >= 0x61 && c <= 0x7A) || c == '_';
    }

    private static boolean isDigit(char c) {
        return c >= 0x30 && c <= 0x39;
    }

    @SuppressWarnings("unused")
    private static boolean isXDigit(char c) {
        return isDigit(c) || (c >= 0x41 && c <= 0x46) || (c >= 0x61 && c <= 0x66);
    }

    private static boolean isAlnum(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private static void error(int line, String fmt, Object... args) {
        String msg = String.format("at line %d: %s", line, fmt, args);
        // System.err.println(msg);
        throw new RuntimeException(msg);
    }

}
