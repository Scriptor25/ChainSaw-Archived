package io.scriptor.chainsaw.runtime;

import io.scriptor.chainsaw.ast.*;
import io.scriptor.chainsaw.ast.stmt.*;
import io.scriptor.chainsaw.ast.expr.*;

public class Interpreter {

    public Value evaluate(Program program) {
        for (var stmt : program)
            evaluate(stmt);

        return evaluateFunction("main");
    }

    public Value evaluateFunction(String id, Value... args) {
        return null;
    }

    public <T> T error(String fmt, Object... args) {
        System.out.printf("Error: %s%n", String.format(fmt, args));
        return null;
    }

    public Value evaluate(Stmt stmt) {

        if (stmt instanceof BodyStmt)
            return evaluate((BodyStmt) stmt);
        if (stmt instanceof ForStmt)
            return evaluate((ForStmt) stmt);
        if (stmt instanceof FuncStmt)
            return evaluate((FuncStmt) stmt);
        if (stmt instanceof IfStmt)
            return evaluate((IfStmt) stmt);
        if (stmt instanceof RetStmt)
            return evaluate((RetStmt) stmt);
        if (stmt instanceof SwitchStmt)
            return evaluate((SwitchStmt) stmt);
        if (stmt instanceof ThingStmt)
            return evaluate((ThingStmt) stmt);
        if (stmt instanceof ValStmt)
            return evaluate((ValStmt) stmt);
        if (stmt instanceof WhileStmt)
            return evaluate((WhileStmt) stmt);

        if (stmt instanceof Expr)
            return evaluate((Expr) stmt);

        return error("");
    }

    public Value evaluate(BodyStmt stmt) {
        System.out.println("BodyStmt");
        return null;
    }

    public Value evaluate(ForStmt stmt) {
        System.out.println("ForStmt");
        return null;
    }

    public Value evaluate(FuncStmt stmt) {

        

        return null;
    }

    public Value evaluate(IfStmt stmt) {
        System.out.println("IfStmt");
        return null;
    }

    public Value evaluate(RetStmt stmt) {
        System.out.println("RetStmt");
        return null;
    }

    public Value evaluate(SwitchStmt stmt) {
        System.out.println("SwitchStmt");
        return null;
    }

    public Value evaluate(ThingStmt stmt) {
        System.out.println("ThingStmt");
        return null;
    }

    public Value evaluate(ValStmt stmt) {
        System.out.println("ValStmt");
        return null;
    }

    public Value evaluate(WhileStmt stmt) {
        System.out.println("WhileStmt");
        return null;
    }

    public Value evaluate(Expr expr) {

        if (expr instanceof AssignExpr)
            return evaluate((AssignExpr) expr);
        if (expr instanceof BinaryExpr)
            return evaluate((BinaryExpr) expr);
        if (expr instanceof CallExpr)
            return evaluate((CallExpr) expr);
        if (expr instanceof CondExpr)
            return evaluate((CondExpr) expr);
        if (expr instanceof ConstExpr)
            return evaluate((ConstExpr) expr);
        if (expr instanceof IdentExpr)
            return evaluate((IdentExpr) expr);
        if (expr instanceof MemberExpr)
            return evaluate((MemberExpr) expr);
        if (expr instanceof UnaryExpr)
            return evaluate((UnaryExpr) expr);

        return error("");
    }

    public Value evaluate(AssignExpr expr) {
        System.out.println("AssignExpr");
        return null;
    }

    public Value evaluate(BinaryExpr expr) {
        System.out.println("BinaryExpr");
        return null;
    }

    public Value evaluate(CallExpr expr) {
        System.out.println("CallExpr");
        return null;
    }

    public Value evaluate(CondExpr expr) {
        System.out.println("CondExpr");
        return null;
    }

    public Value evaluate(ConstExpr expr) {
        System.out.println("ConstExpr");
        return null;
    }

    public Value evaluate(IdentExpr expr) {
        System.out.println("IdentExpr");
        return null;
    }

    public Value evaluate(MemberExpr expr) {
        System.out.println("MemberExpr");
        return null;
    }

    public Value evaluate(UnaryExpr expr) {
        System.out.println("UnaryExpr");
        return null;
    }

}
