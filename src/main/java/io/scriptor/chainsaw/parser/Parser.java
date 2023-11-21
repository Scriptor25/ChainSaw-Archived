package io.scriptor.chainsaw.parser;

import io.scriptor.chainsaw.Util;
import io.scriptor.chainsaw.ast.Program;
import io.scriptor.chainsaw.ast.expr.*;
import io.scriptor.chainsaw.ast.stmt.*;
import io.scriptor.chainsaw.lexer.Token;
import io.scriptor.chainsaw.lexer.TokenType;

import java.util.List;
import java.util.Vector;

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

    private boolean nextType(TokenType type) {
        return nextType(0, type);
    }

    private boolean nextType(int off, TokenType type) {
        if (!ok(off))
            return false;

        return next(off).type == type;
    }

    private boolean nextValue(String value) {
        return nextValue(0, value);
    }

    private boolean nextValue(int off, String value) {
        if (!ok(off))
            return false;

        return next(off).value.equals(value);
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
        if (!(ok() && nextValue(value)))
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
        return Util.error("at line %d: %s", line, msg);
    }

    public Param parseParam() {
        var param = new Param();
        param.name = expect(TokenType.IDENTIFIER).value;
        expect(TokenType.COLON);
        param.type = expect(TokenType.IDENTIFIER).value;
        return param;
    }

    public void parseParams(List<Param> params) {
        do {
            if (!nextType(TokenType.IDENTIFIER))
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

        if (findAndEat(TokenType.SEMICOLON))
            return null;

        if (nextType(TokenType.BRACE_OPEN))
            return parseBodyStmt();

        if (nextValue("inc"))
            return parseIncStmt();

        if (nextValue("thing"))
            return parseThingStmt();

        if (nextValue("if"))
            return parseIfStmt();

        if (nextValue("switch"))
            return parseSwitchStmt();

        if (nextValue("while"))
            return parseWhileStmt();

        if (nextValue("for"))
            return parseForStmt();

        if (nextValue("ret"))
            return parseRetStmt();

        if (nextType(TokenType.IDENTIFIER) && nextType(1, TokenType.IDENTIFIER))
            return parseVarStmt();

        if (nextType(TokenType.DOLLAR)
                || (nextType(TokenType.PAREN_OPEN)
                        && (nextType(2, TokenType.PAREN_CLOSE) || nextType(3, TokenType.PAREN_CLOSE)))
                ||
                (nextType(TokenType.IDENTIFIER) &&
                        (nextType(1, TokenType.BRACE_OPEN) ||
                                nextType(1, TokenType.BRACKET_OPEN) ||
                                nextType(1, TokenType.SEMICOLON) ||
                                (nextType(1, TokenType.MINUS) && nextType(2, TokenType.GREATER)) ||
                                (nextType(1, TokenType.COLON) && nextType(2, TokenType.IDENTIFIER)))))
            return parseFuncStmt();

        var expr = parseExpr();
        if (!nextType(TokenType.PAREN_CLOSE))
            expect(TokenType.SEMICOLON);

        return expr;
    }

    public IncStmt parseIncStmt() {
        var stmt = new IncStmt();

        expect("inc");
        stmt.path = expect(TokenType.STRING).value;
        expect(TokenType.SEMICOLON);

        return stmt;
    }

    public ThingStmt parseThingStmt() {
        var stmt = new ThingStmt();

        expect("thing");
        expect(TokenType.COLON);

        stmt.name = expect(TokenType.IDENTIFIER).value;

        if (findAndEat(TokenType.SEMICOLON))
            return stmt;

        expect(TokenType.BRACE_OPEN);
        stmt.fields = new Vector<>();
        parseParams(stmt.fields);
        expect(TokenType.BRACE_CLOSE);

        return stmt;
    }

    public FunctionStmt parseFuncStmt() {
        var stmt = new FunctionStmt();

        stmt.isOperator = findAndEat(TokenType.PAREN_OPEN);
        if (stmt.isOperator) {
            stmt.name = eat().value;
            if (!findAndEat(TokenType.PAREN_CLOSE)) {
                stmt.name += eat().value;
                expect(TokenType.PAREN_CLOSE);
            }
        } else {
            stmt.isConstructor = findAndEat(TokenType.DOLLAR);
            stmt.name = expect(TokenType.IDENTIFIER).value;
        }

        if (findAndEat(TokenType.COLON)) { // return type
            if (stmt.isConstructor)
                return error(next().line, "constructor must not have a type specified");
            stmt.resultType = expect(TokenType.IDENTIFIER).value;
        }

        if (findAndEat(TokenType.BRACKET_OPEN)) { // parameters
            parseParams(stmt.parameters);
            expect(TokenType.BRACKET_CLOSE);
        }

        if (findAndEat(TokenType.MINUS, TokenType.GREATER)) { // member
            stmt.superType = expect(TokenType.IDENTIFIER).value;
        }

        if (nextValue("var")) { // vararg
            eat();
            stmt.isVararg = true;
        }

        if (findAndEat(TokenType.SEMICOLON))
            return stmt;

        stmt.implementation = parseBodyStmt();

        return stmt;
    }

    public BodyStmt parseBodyStmt() {
        var stmt = new BodyStmt();

        expect(TokenType.BRACE_OPEN);
        while (!findAndEat(TokenType.BRACE_CLOSE))
            stmt.statements.add(parseStmt());

        return stmt;
    }

    public IfStmt parseIfStmt() {
        var stmt = new IfStmt();

        expect("if");
        expect(TokenType.PAREN_OPEN);
        stmt.condition = parseExpr();
        expect(TokenType.PAREN_CLOSE);
        stmt.thenStmt = parseStmt();
        if (findAndEat("else"))
            stmt.elseStmt = parseStmt();

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
        stmt.entry = parseStmt();
        stmt.condition = parseExpr();
        expect(TokenType.SEMICOLON);
        stmt.next = parseStmt();
        expect(TokenType.PAREN_CLOSE);
        stmt.body = parseStmt();

        return stmt;
    }

    public ReturnStmt parseRetStmt() {
        expect("ret");
        if (findAndEat(TokenType.SEMICOLON))
            return new ReturnStmt(null);

        var value = parseExpr();
        expect(TokenType.SEMICOLON);
        return new ReturnStmt(value);
    }

    public VariableStmt parseVarStmt() {
        var stmt = new VariableStmt();

        stmt.type = expect(TokenType.IDENTIFIER).value;
        stmt.name = expect(TokenType.IDENTIFIER).value;

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

            assigne = new AssignmentExpr(assigne, value);
        }

        return assigne;
    }

    public Expr parseCondExpr() {
        var condition = parseBinaryExpr();

        if (findAndEat(TokenType.QUEST)) {
            var istrue = parseExpr();
            expect(TokenType.COLON);
            var isfalse = parseExpr();

            condition = new ConditionExpr(condition, istrue, isfalse);
        }

        return condition;
    }

    public Expr parseBinaryExpr() {
        return parseAndBinaryExpr();
    }

    public Expr parseAndBinaryExpr() {
        var left = parseOrBinaryExpr();

        while (findAndEat(TokenType.AND, TokenType.AND)) {

            var right = parseOrBinaryExpr();

            left = new BinaryExpr(left, right, "&&");
        }

        return left;
    }

    public Expr parseOrBinaryExpr() {
        var left = parseCmpBinaryExpr();

        while (findAndEat(TokenType.PIPE, TokenType.PIPE)) {

            var right = parseCmpBinaryExpr();

            left = new BinaryExpr(left, right, "||");
        }

        return left;
    }

    public Expr parseCmpBinaryExpr() {
        var left = parseSumBinaryExpr();

        while ((nextType(TokenType.EQUAL) &&
                nextType(1, TokenType.EQUAL))
                || (nextType(TokenType.EXCLAM) &&
                        nextType(1, TokenType.EQUAL))
                || nextType(TokenType.LESS)
                || nextType(TokenType.GREATER)) {

            var operator = eat().value;
            if (operator.equals("=") || nextType(TokenType.EQUAL))
                operator += expect(TokenType.EQUAL).value;

            var right = parseSumBinaryExpr();

            left = new BinaryExpr(left, right, operator);
        }

        return left;
    }

    public Expr parseSumBinaryExpr() {
        var left = parseProBinaryExpr();

        while (nextType(TokenType.PLUS) || nextType(TokenType.MINUS)) {

            var operator = eat().value;
            boolean assign = findAndEat(TokenType.EQUAL);
            var right = parseProBinaryExpr();

            if (assign)
                left = new AssignmentExpr(left, new BinaryExpr(left, right, operator, true));
            else
                left = new BinaryExpr(left, right, operator);
        }

        return left;
    }

    public Expr parseProBinaryExpr() {
        var left = parseUnaryExpr();

        while (nextType(TokenType.ASTER) || nextType(TokenType.SLASH)) {

            var operator = eat().value;
            boolean assign = findAndEat(TokenType.EQUAL);
            var right = parseUnaryExpr();

            if (assign)
                left = new AssignmentExpr(left, new BinaryExpr(left, right, operator, true));
            else
                left = new BinaryExpr(left, right, operator);
        }

        return left;
    }

    public Expr parseUnaryExpr() {
        var expr = parseCallExpr();

        if ((nextType(TokenType.PLUS) && nextType(1, TokenType.PLUS)) ||
                (nextType(TokenType.MINUS) && nextType(1, TokenType.MINUS))) {

            var operator = eat().value;
            expect(operator);

            expr = new AssignmentExpr(expr,
                    new BinaryExpr(expr, new ConstantExpr("1", ConstantExpr.ConstantType.NUMBER), operator, true));
        }

        return expr;
    }

    public Expr parseCallExpr() {
        var expr = parseMemberExpr();

        while (findAndEat(TokenType.PAREN_OPEN)) {
            var cexpr = new CallExpr();
            cexpr.function = expr;

            do {
                if (nextType(TokenType.PAREN_CLOSE))
                    break;
                cexpr.args.add(parseExpr());
            } while (findAndEat(TokenType.COMMA));
            expect(TokenType.PAREN_CLOSE);

            expr = cexpr;

            if (findAndEat(TokenType.PERIOD)) {
                var mexpr = new MemberExpr();
                mexpr.thing = cexpr;
                mexpr.member = parseCallExpr();
                expr = mexpr;
            }
        }

        return expr;
    }

    public Expr parseMemberExpr() {
        var expr = parsePrimaryExpr();

        while (findAndEat(TokenType.PERIOD)) {
            var mexpr = new MemberExpr();
            mexpr.thing = expr;
            mexpr.member = parsePrimaryExpr();
            expr = mexpr;
        }

        return expr;
    }

    public Expr parsePrimaryExpr() {
        if (!ok())
            return null;

        var token = eat();
        switch (token.type) {
            case IDENTIFIER:
                return new IdentifierExpr(token.value);
            case NUMBER:
                return new ConstantExpr(token.value, ConstantExpr.ConstantType.NUMBER);
            case CHAR:
                return new ConstantExpr(token.value, ConstantExpr.ConstantType.CHAR);
            case STRING:
                return new ConstantExpr(token.value, ConstantExpr.ConstantType.STRING);
            case PAREN_OPEN: {
                var expr = parseExpr();
                expect(TokenType.PAREN_CLOSE);
                return expr;
            }
            case MINUS:
                return new UnaryExpr("-", parseUnaryExpr());
            case EXCLAM:
                return new UnaryExpr("!", parseUnaryExpr());
            case TILDE:
                return new UnaryExpr("~", parseUnaryExpr());

            default:
                return error(token.line, "undefined token type '%s' ('%s')", token.type, token.value);
        }
    }
}
