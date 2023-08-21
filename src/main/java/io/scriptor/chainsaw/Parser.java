package io.scriptor.chainsaw;

import java.util.List;
import java.util.Vector;

import io.scriptor.chainsaw.ast.BodyStmt;
import io.scriptor.chainsaw.ast.CallExpr;
import io.scriptor.chainsaw.ast.Expr;
import io.scriptor.chainsaw.ast.FuncStmt;
import io.scriptor.chainsaw.ast.IdentExpr;
import io.scriptor.chainsaw.ast.Param;
import io.scriptor.chainsaw.ast.Program;
import io.scriptor.chainsaw.ast.Stmt;
import io.scriptor.chainsaw.ast.ThingStmt;

public class Parser {

    private final List<Token> mTokens;
    private int mIndex = 0;

    public Parser(List<Token> tokens) {
        this.mTokens = tokens;
    }

    private Token next() {
        return next(0);
    }

    private Token next(int off) {
        if (!ok(off))
            return null;
        return mTokens.get(mIndex + off);
    }

    private Token eat() {
        if (!ok())
            return null;
        return mTokens.get(mIndex++);
    }

    private Token expect(TokenType type) {
        if (!ok())
            return null;
        var token = eat();
        if (token.type == type)
            return token;

        return error(
                token.line,
                "unexpected token type '%s' ('%s'), expected type '%s'",
                token.type,
                token.value,
                type);
    }

    private Token expect(String value) {
        if (!ok())
            return null;
        var token = eat();
        if (token.value.equals(value))
            return token;

        return error(
                token.line,
                "unexpected value '%s' ('%s'), expected '%s'",
                token.value,
                token.type,
                value);
    }

    private boolean findAndEat(TokenType type) {
        if (!ok() || next().type != type)
            return false;

        eat();
        return true;
    }

    private boolean findAndEat(TokenType... types) {
        if (types == null || types.length == 0 || !ok(types.length - 1))
            return false;

        for (int off = 0; off < types.length; off++)
            if (next(off).type != types[off])
                return false;

        for (int i = 0; i < types.length; i++)
            eat();

        return true;
    }

    private boolean ok() {
        return ok(0);
    }

    private boolean ok(int off) {
        return (mIndex + off) < mTokens.size();
    }

    private static <T> T error(int line, String fmt, Object... args) {
        String msg = String.format("at line %d: %s", line, String.format(fmt, args));
        // System.err.println(msg);
        throw new RuntimeException(msg);
    }

    public Param parseParam() {
        Param param = new Param();
        param.ident = expect(TokenType.IDENTIFIER).value;
        expect(TokenType.COLON);
        param.type = expect(TokenType.IDENTIFIER).value;
        return param;
    }

    public void parseParams(List<Param> params) {
        do {
            if (next().type != TokenType.IDENTIFIER)
                break;
            params.add(parseParam());
        } while (findAndEat(TokenType.COMMA));
    }

    public Program parseProgram() {
        mIndex = 0;

        Program program = new Program();
        while (ok())
            program.add(parseStmt());

        return program;
    }

    public Stmt parseStmt() {

        if (next().value.equals("thing"))
            return parseThingStmt();

        return parseFuncStmt();
    }

    public ThingStmt parseThingStmt() {
        ThingStmt stmt = new ThingStmt();

        expect("thing");
        expect(TokenType.COLON);

        stmt.ident = expect(TokenType.IDENTIFIER).value;

        if (findAndEat(TokenType.SEMICOLON))
            return stmt;

        expect(TokenType.BRACE_OPEN);
        stmt.params = new Vector<>();
        parseParams(stmt.params);
        expect(TokenType.BRACE_CLOSE);

        return stmt;
    }

    public FuncStmt parseFuncStmt() {
        FuncStmt stmt = new FuncStmt();

        stmt.ident = expect(TokenType.IDENTIFIER).value;
        if (findAndEat(TokenType.COLON))
            stmt.type = expect(TokenType.IDENTIFIER).value;
        if (findAndEat(TokenType.LESS, TokenType.LESS)) {
            expect(TokenType.BRACKET_OPEN);
            parseParams(stmt.params);
            expect(TokenType.BRACKET_CLOSE);
        }
        if (next().value.equals("var")) {
            eat();
            stmt.vararg = true;
        }
        if (findAndEat(TokenType.SEMICOLON))
            return stmt;

        stmt.impl = parseBodyStmt();

        return stmt;
    }

    public BodyStmt parseBodyStmt() {
        BodyStmt stmt = new BodyStmt();

        expect(TokenType.BRACE_OPEN);
        while (!findAndEat(TokenType.BRACE_CLOSE))
            stmt.stmts.add(parseStmt());

        return stmt;
    }

    public Expr parseExpr() {
        return parseCallExpr();
    }

    public Expr parseCallExpr() {

        CallExpr expr = new CallExpr();
        expr.function = expect(TokenType.IDENTIFIER).value;

        expect(TokenType.PAREN_OPEN);
        do {
            expr.args.add(parseExpr());
        } while (findAndEat(TokenType.COMMA));
        expect(TokenType.PAREN_CLOSE);

        return expr;
    }

    public Expr parseBinaryExpr() {
        return parsePrimaryExpr();
    }

    public Expr parsePrimaryExpr() {
        var token = eat();

        switch (token.type) {
            case IDENTIFIER:
                return new IdentExpr(token.value);

            default:
                return error(token.line, "undefined token %s");
        }
    }
}
