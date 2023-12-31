package io.scriptor.chainsaw.lexer;

public class Token {

    public TokenType type = TokenType.UNDEFINED;
    public String value = null;
    public int line = 0;

    public Token type(TokenType type) {
        this.type = type;
        return this;
    }

    public Token value(String value) {
        this.value = value;
        return this;
    }

    public Token value(char value) {
        this.value = String.valueOf(value);
        return this;
    }

    public Token line(int line) {
        this.line = line;
        return this;
    }

    @Override
    public String toString() {
        return String.format("{ \"type\": \"%s\", \"value\": \"%s\", \"line\": %d }", type, value, line);
    }

}
