package io.scriptor.chainsaw;

import java.util.List;
import java.util.Vector;

public class Lexer {

    private Lexer() {
    }

    public static List<Token> tokenize(String source) {
        StringEater eater = new StringEater(source);

        List<Token> tokens = new Vector<>();
        int line = 1;

        while (eater.ok()) {
            char c = eater.eat();

            // unused
            if (isIgnorable(c)) {
                if (c == '\n')
                    line++;
                continue;
            }

            // comment
            if (c == '#') {
                // single line
                if (eater.next() == '#') {
                    while (eater.eat() != '\n')
                        ;
                    line++;
                    continue;
                }

                // multi
                while ((c = eater.eat()) != '#')
                    if (c == '\n')
                        line++;
                continue;
            }

            // identifier
            if (isAlpha(c)) {
                StringBuilder ident = new StringBuilder().append(c);

                while (isAlnum(eater.next()))
                    ident.append(eater.eat());

                tokens.add(new Token()
                        .type(TokenType.IDENTIFIER)
                        .value(ident.toString())
                        .line(line));

                continue;
            }

            // number
            if (isDigit(c) || (c == '.' && isDigit(eater.next()))) {
                boolean hasPeriod = c == '.';
                StringBuilder num = new StringBuilder().append(c);
                while (isDigit(eater.next()) || eater.next() == '.') {
                    c = eater.eat();
                    if (c == '.') {
                        if (hasPeriod)
                            error(line, "too many period chars in number");
                        hasPeriod = true;
                    }

                    num.append(c);
                }

                tokens.add(new Token()
                        .type(TokenType.NUMBER)
                        .value(num.toString())
                        .line(line));

                continue;
            }

            if (c == '"') {
                StringBuilder string = new StringBuilder();
                int startLine = line;

                while (eater.next() != '"') {
                    if (eater.next() == '\n')
                        line++;

                    if (eater.next() == '\\') {
                        eater.eat();

                        switch (eater.eat()) {

                            case 't':
                                string.append('\t');
                                break;
                            case 'b':
                                string.append('\b');
                                break;
                            case 'n':
                                string.append('\n');
                                break;
                            case 'r':
                                string.append('\r');
                                break;
                            case 'f':
                                string.append('\f');
                                break;

                            default:
                                string.append(eater.next(-1));
                                break;
                        }
                    } else
                        string.append(eater.eat());
                }

                eater.eat();

                tokens.add(new Token()
                        .type(TokenType.STRING)
                        .value(string.toString())
                        .line(startLine));

                continue;
            }

            if (c == '\'') {
                StringBuilder character = new StringBuilder();

                while (eater.next() != '\'') {
                    if (eater.next() == '\\') {
                        eater.eat();

                        switch (eater.eat()) {

                            case 't':
                                character.append('\t');
                                break;
                            case 'b':
                                character.append('\b');
                                break;
                            case 'n':
                                character.append('\n');
                                break;
                            case 'r':
                                character.append('\r');
                                break;
                            case 'f':
                                character.append('\f');
                                break;

                            default:
                                character.append(eater.next(-1));
                                break;
                        }
                    } else
                        character.append(eater.eat());
                }

                eater.eat();

                if (character.length() != 1)
                    error(line, "length of character must be exactly one: '%s'", character);

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
        throw new RuntimeException(msg);
    }

}
