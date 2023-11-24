package chainsaw;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class Lexer {

    private Lexer() {
    }

    public static List<Token> tokenize(String source) {
        if (source.trim().isEmpty())
            return Collections.emptyList();

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
                    while (eater.ok() && eater.eat() != '\n')
                        ;
                    line++;
                    continue;
                }

                // multi
                while (eater.ok() && (c = eater.eat()) != '#')
                    if (c == '\n')
                        line++;
                continue;
            }

            // identifier
            if (isAlpha(c)) {
                StringBuilder ident = new StringBuilder().append(c);

                while (eater.ok() && isAlnum(eater.next()))
                    ident.append(eater.eat());

                tokens.add(new Token()
                        .type(TokenType.IDENTIFIER)
                        .value(ident.toString())
                        .line(line));

                continue;
            }

            // number
            if (isDigit(c) || (c == '.' && isDigit(eater.next()))) {
                boolean isFloat = c == '.';
                StringBuilder num = new StringBuilder().append(c);
                while (eater.ok() && (isDigit(eater.next()) || eater.next() == '.')) {
                    c = eater.eat();
                    if (c == '.') {
                        if (isFloat)
                            error(line, "too many period chars in number");
                        isFloat = true;
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

                while (eater.ok() && eater.next() != '"') {
                    if (eater.next() == '\n')
                        line++;
                    next(eater, string);
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

                while (eater.ok() && eater.next() != '\'')
                    next(eater, character);

                eater.eat();

                if (character.length() != 1)
                    error(line, "length of character must be exactly one: '%s'", character);

                tokens.add(new Token()
                        .type(TokenType.CHAR)
                        .value(character.toString())
                        .line(line));

                continue;
            }

            TokenType type = switch (c) {
                case '(' -> TokenType.PAREN_OPEN;
                case ')' -> TokenType.PAREN_CLOSE;
                case '{' -> TokenType.BRACE_OPEN;
                case '}' -> TokenType.BRACE_CLOSE;
                case '[' -> TokenType.BRACKET_OPEN;
                case ']' -> TokenType.BRACKET_CLOSE;
                case '.' -> TokenType.PERIOD;
                case ':' -> TokenType.COLON;
                case ',' -> TokenType.COMMA;
                case ';' -> TokenType.SEMICOLON;
                case '+' -> TokenType.PLUS;
                case '-' -> TokenType.MINUS;
                case '*' -> TokenType.ASTER;
                case '/' -> TokenType.SLASH;
                case '^' -> TokenType.CARET;
                case '!' -> TokenType.EXCLAM;
                case '$' -> TokenType.DOLLAR;
                case '%' -> TokenType.PERCENT;
                case '&' -> TokenType.AND;
                case '=' -> TokenType.EQUAL;
                case '?' -> TokenType.QUEST;
                case '~' -> TokenType.TILDE;
                case '<' -> TokenType.LESS;
                case '>' -> TokenType.GREATER;
                case '|' -> TokenType.PIPE;
                default -> error(line, "undefined char '%s'", c);
            };

            tokens.add(new Token()
                    .type(type)
                    .value(c)
                    .line(line));
        }

        return tokens;
    }

    private static void next(StringEater eater, StringBuilder builder) {
        if (eater.next() == '\\') {
            eater.eat();

            builder.append(switch (eater.eat()) {
                case 't' -> '\t';
                case 'b' -> '\b';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 'f' -> '\f';
                default -> eater.next(-1);
            });
        } else
            builder.append(eater.eat());
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

    private static boolean isAlnum(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private static <T> T error(int line, String fmt, Object... args) {
        String msg = String.format("at line %d: %s", line, String.format(fmt, args));
        throw new RuntimeException(msg);
    }

}
