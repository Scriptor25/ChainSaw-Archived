package io.scriptor.chainsaw;

import java.util.List;
import java.util.Vector;

import io.scriptor.chainsaw.ast.*;
import io.scriptor.chainsaw.ast.expr.*;
import io.scriptor.chainsaw.ast.stmt.*;

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

    private boolean findAndEat(String value) {
        if (!(ok() && next().value.equals(value)))
            return false;

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
        var msg = String.format(fmt, args);
        var err = String.format("at line %d: %s", line, msg);
        System.err.println(err);
        throw new RuntimeException(err);
    }

    public Param parseParam() {
        var param = new Param();
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

        var program = new Program();
        while (ok())
            program.add(parseStmt());

        return program;
    }

    public Stmt parseStmt() {

        if (next().type == TokenType.SEMICOLON)
            return null;

        if (next().type == TokenType.BRACE_OPEN)
            return parseBodyStmt();

        if (next().value.equals("thing"))
            return parseThingStmt();

        if (next().value.equals("if"))
            return parseIfStmt();

        if (next().value.equals("switch"))
            return parseSwitchStmt();

        if (next().value.equals("while"))
            return parseWhileStmt();

        if (next().value.equals("for"))
            return parseForStmt();

        if (next().value.equals("ret"))
            return parseRetStmt();

        if (next().type.equals(TokenType.IDENTIFIER) && next(1).type.equals(TokenType.IDENTIFIER))
            return parseValStmt();

        // functions and constructors

        if (findAndEat(TokenType.DOLLAR))
            return parseFuncStmt(true);

        if (next().type == TokenType.IDENTIFIER &&
                (next(1).type == TokenType.BRACE_OPEN ||
                        next(1).type == TokenType.SEMICOLON ||
                        (next(1).type == TokenType.MINUS && next(2).type == TokenType.GREATER) ||
                        (next(1).type == TokenType.COLON && next(2).type == TokenType.IDENTIFIER) ||
                        (next(1).type == TokenType.LESS && next(2).type == TokenType.LESS)))
            return parseFuncStmt(false);

        var expr = parseExpr();
        if (next().type != TokenType.PAREN_CLOSE)
            expect(TokenType.SEMICOLON);

        return expr;
    }

    public ThingStmt parseThingStmt() {
        var stmt = new ThingStmt();

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

    public FuncStmt parseFuncStmt(boolean constructor) {
        var stmt = new FuncStmt();

        stmt.ident = expect(TokenType.IDENTIFIER).value;
        stmt.constructor = constructor;
        if (findAndEat(TokenType.COLON)) { // return type
            if (constructor)
                return error(next().line, "constructor must not have a type specified");
            stmt.type = expect(TokenType.IDENTIFIER).value;
        }
        if (findAndEat(TokenType.LESS, TokenType.LESS)) { // parameters
            expect(TokenType.BRACKET_OPEN);
            parseParams(stmt.params);
            expect(TokenType.BRACKET_CLOSE);
        }
        if (next().value.equals("var")) { // vararg
            eat();
            stmt.vararg = true;
        }
        if (findAndEat(TokenType.MINUS, TokenType.GREATER)) { // thing type
            stmt.memberOf = expect(TokenType.IDENTIFIER).value;
        }
        if (findAndEat(TokenType.SEMICOLON))
            return stmt;

        stmt.impl = parseBodyStmt();

        return stmt;
    }

    public BodyStmt parseBodyStmt() {
        var stmt = new BodyStmt();

        expect(TokenType.BRACE_OPEN);
        while (!findAndEat(TokenType.BRACE_CLOSE))
            stmt.stmts.add(parseStmt());

        return stmt;
    }

    public IfStmt parseIfStmt() {
        var stmt = new IfStmt();

        expect("if");
        expect(TokenType.PAREN_OPEN);
        stmt.condition = parseExpr();
        expect(TokenType.PAREN_CLOSE);
        stmt.isTrue = parseStmt();
        if (findAndEat("else"))
            stmt.isFalse = parseStmt();

        return stmt;
    }

    public SwitchStmt parseSwitchStmt() {
        var stmt = new SwitchStmt();

        expect("switch");
        expect(TokenType.PAREN_OPEN);
        stmt.condition = parseExpr();
        expect(TokenType.PAREN_CLOSE);
        expect(TokenType.BRACE_OPEN);
        while (!findAndEat(TokenType.BRACE_CLOSE)) {

            boolean isDefault = findAndEat("default");
            Expr value = null;
            if (!isDefault) {
                expect("case");
                value = parseExpr();
            }
            expect(TokenType.COLON);
            Stmt next = parseStmt();

            if (isDefault) {
                if (stmt.defaultCase != null)
                    return error(next().line, "cannot define a default case for a switch stmt twice");

                stmt.defaultCase = next;
            } else {
                stmt.cases.put(value, next);
            }
        }

        return stmt;
    }

    public WhileStmt parseWhileStmt() {
        var stmt = new WhileStmt();

        expect("while");
        expect(TokenType.PAREN_OPEN);
        stmt.condition = parseExpr();
        expect(TokenType.PAREN_CLOSE);
        stmt.body = parseStmt();

        return stmt;
    }

    public ForStmt parseForStmt() {
        var stmt = new ForStmt();

        expect("for");
        expect(TokenType.PAREN_OPEN);
        stmt.before = parseStmt();
        stmt.condition = parseExpr();
        expect(TokenType.SEMICOLON);
        stmt.loop = parseStmt();
        expect(TokenType.PAREN_CLOSE);
        stmt.body = parseStmt();

        return stmt;
    }

    public RetStmt parseRetStmt() {
        expect("ret");
        var value = parseExpr();
        expect(TokenType.SEMICOLON);
        return new RetStmt(value);
    }

    public ValStmt parseValStmt() {
        var stmt = new ValStmt();

        stmt.type = expect(TokenType.IDENTIFIER).value;
        stmt.ident = expect(TokenType.IDENTIFIER).value;

        if (findAndEat(TokenType.SEMICOLON))
            return stmt;

        expect(TokenType.EQUAL);
        stmt.value = parseExpr();
        expect(TokenType.SEMICOLON);

        return stmt;
    }

    public Expr parseExpr() {
        return parseAssignExpr();
    }

    public Expr parseAssignExpr() {
        var assigne = parseCondExpr();

        if (findAndEat(TokenType.EQUAL)) {
            var value = parseExpr();

            assigne = new AssignExpr(assigne, value);
        }

        return assigne;
    }

    public Expr parseCondExpr() {
        var condition = parseBinaryExpr();

        if (findAndEat(TokenType.QUEST)) {
            var istrue = parseExpr();
            expect(TokenType.COLON);
            var isfalse = parseExpr();

            condition = new CondExpr(condition, istrue, isfalse);
        }

        return condition;
    }

    public Expr parseBinaryExpr() {
        return parseAndBinaryExpr();
    }

    public Expr parseAndBinaryExpr() {
        var left = parseOrBinaryExpr();

        if (findAndEat(TokenType.AND, TokenType.AND)) {

            var right = parseExpr();

            left = new BinaryExpr(left, right, "&&");
        }

        return left;
    }

    public Expr parseOrBinaryExpr() {
        var left = parseCmpBinaryExpr();

        if (findAndEat(TokenType.PIPE, TokenType.PIPE)) {

            var right = parseExpr();

            left = new BinaryExpr(left, right, "||");
        }

        return left;
    }

    public Expr parseCmpBinaryExpr() {
        var left = parseSumBinaryExpr();

        if ((next().type == TokenType.EQUAL &&
                next(1).type == TokenType.EQUAL) ||
                next().type == TokenType.LESS ||
                next().type == TokenType.GREATER) {

            var operator = eat().value;
            if (operator.equals("=") || next().type == TokenType.EQUAL)
                operator += expect(TokenType.EQUAL).value;

            var right = parseExpr();

            left = new BinaryExpr(left, right, operator);
        }

        return left;
    }

    public Expr parseSumBinaryExpr() {
        var left = parseProBinaryExpr();

        if (next().type == TokenType.PLUS || next().type == TokenType.MINUS) {

            var operator = eat().value;
            boolean assign = findAndEat(TokenType.EQUAL);
            var right = parseSumBinaryExpr();

            if (assign)
                left = new AssignExpr(left, new BinaryExpr(left, right, operator));
            else
                left = new BinaryExpr(left, right, operator);
        }

        return left;
    }

    public Expr parseProBinaryExpr() {
        var left = parseUnaryExpr();

        if (next().type == TokenType.ASTER || next().type == TokenType.SLASH) {

            var operator = eat().value;
            boolean assign = findAndEat(TokenType.EQUAL);
            var right = parseProBinaryExpr();

            if (assign)
                left = new AssignExpr(left, new BinaryExpr(left, right, operator));
            else
                left = new BinaryExpr(left, right, operator);
        }

        return left;
    }

    public Expr parseUnaryExpr() {
        var expr = parseCallExpr();

        if ((next().type == TokenType.PLUS && next(1).type == TokenType.PLUS) ||
                next().type == TokenType.MINUS && next(1).type == TokenType.MINUS) {

            var operator = eat().value;
            expect(operator);

            expr = new AssignExpr(expr, new BinaryExpr(expr, new ConstExpr("1", ConstType.NUMBER), operator));
        }

        return expr;
    }

    public Expr parseCallExpr() {
        var expr = parseMemberExpr();

        if (findAndEat(TokenType.PAREN_OPEN)) {
            var cexpr = new CallExpr();
            cexpr.function = ((IdentExpr) expr).value;

            do {
                if (next().type == TokenType.PAREN_CLOSE)
                    break;
                cexpr.args.add(parseExpr());
            } while (findAndEat(TokenType.COMMA));
            expect(TokenType.PAREN_CLOSE);

            expr = cexpr;
        }

        return expr;
    }

    public Expr parseMemberExpr() {
        var expr = parsePrimaryExpr();

        if (findAndEat(TokenType.PERIOD)) {
            var mexpr = new MemberExpr();
            mexpr.thing = ((IdentExpr) expr).value;
            mexpr.member = parseCallExpr();
            expr = mexpr;
        }

        return expr;
    }

    public Expr parsePrimaryExpr() {
        var token = eat();

        switch (token.type) {
            case IDENTIFIER:
                return new IdentExpr(token.value);
            case STRING:
                return new ConstExpr(token.value, ConstType.STRING);
            case NUMBER:
                return new ConstExpr(token.value, ConstType.NUMBER);
            case CHAR:
                return new ConstExpr(token.value, ConstType.CHAR);
            case PAREN_OPEN: {
                var expr = parseExpr();
                expect(TokenType.PAREN_CLOSE);
                return expr;
            }
            case MINUS:
                return new UnaryExpr("-", parseExpr());
            case EXCLAM:
                return new UnaryExpr("!", parseExpr());

            default:
                return error(token.line, "undefined token type '%s' ('%s')", token.type, token.value);
        }
    }
}
