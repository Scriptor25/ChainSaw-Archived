package io.scriptor.chainsaw.runtime;

import java.util.HashMap;
import java.util.Map;

import io.scriptor.chainsaw.ast.*;
import io.scriptor.chainsaw.ast.expr.*;
import io.scriptor.chainsaw.ast.stmt.*;

public class Interpreter {

    private Program mProgram;
    private Environment mEnvironment = new Environment();

    public Interpreter(Program program) {
        this.mProgram = program;
    }

    public void evaluate() {
        mEnvironment.reset();

        for (var stmt : mProgram)
            evaluateStmt(stmt);

        System.out.println(mEnvironment);
    }

    private Type evaluateType(String type) {
        switch (type) {
            case "int":
                return mEnvironment.getIntType();
            case "float":
                return mEnvironment.getFloatType();

            default:
                return mEnvironment.getThingType(type);
        }
    }

    private Value evaluateStmt(Stmt stmt) {
        if (stmt instanceof BodyStmt)
            return evaluateBodyStmt((BodyStmt) stmt);
        if (stmt instanceof FuncStmt)
            return evaluateFuncStmt((FuncStmt) stmt);
        if (stmt instanceof IfStmt)
            return evaluateIfStmt((IfStmt) stmt);
        if (stmt instanceof RetStmt)
            return evaluateRetStmt((RetStmt) stmt);
        if (stmt instanceof ThingStmt)
            return evaluateThingStmt((ThingStmt) stmt);
        if (stmt instanceof ValStmt)
            return evaluateValStmt((ValStmt) stmt);

        return evaluateExpr((Expr) stmt);
    }

    private Value evaluateBodyStmt(BodyStmt stmt) {
        return Util.error("evaluation for %s not yet implemented", stmt.getClass().getSimpleName());
    }

    private Value evaluateFuncStmt(FuncStmt stmt) {
        return Util.error("evaluation for %s not yet implemented", stmt.getClass().getSimpleName());
    }

    private Value evaluateIfStmt(IfStmt stmt) {
        return Util.error("evaluation for %s not yet implemented", stmt.getClass().getSimpleName());
    }

    private Value evaluateRetStmt(RetStmt stmt) {
        return Util.error("evaluation for %s not yet implemented", stmt.getClass().getSimpleName());
    }

    private Value evaluateThingStmt(ThingStmt stmt) {

        Map<String, Type> fields = new HashMap<>();
        fields.put("", null);
        fields.put("", null);

        ThingType.get(mEnvironment, stmt.ident, fields);

        return null;
    }

    private Value evaluateValStmt(ValStmt stmt) {
        return Util.error("evaluation for %s not yet implemented", stmt.getClass().getSimpleName());
    }

    private Value evaluateExpr(Expr expr) {

        if (expr instanceof BinaryExpr)
            return evaluateBinaryExpr((BinaryExpr) expr);
        if (expr instanceof CallExpr)
            return evaluateCallExpr((CallExpr) expr);
        if (expr instanceof ConstExpr)
            return evaluateConstExpr((ConstExpr) expr);
        if (expr instanceof IdentExpr)
            return evaluateIdentExpr((IdentExpr) expr);
        if (expr instanceof MemberExpr)
            return evaluateMemberExpr((MemberExpr) expr);

        return Util.error("not implemented: %s", expr);
    }

    private Value evaluateBinaryExpr(BinaryExpr expr) {
        return Util.error("evaluation for %s not yet implemented", expr.getClass().getSimpleName());
    }

    private Value evaluateCallExpr(CallExpr expr) {
        return Util.error("evaluation for %s not yet implemented", expr.getClass().getSimpleName());
    }

    private Value evaluateConstExpr(ConstExpr expr) {
        return Util.error("evaluation for %s not yet implemented", expr.getClass().getSimpleName());
    }

    private Value evaluateIdentExpr(IdentExpr expr) {
        return Util.error("evaluation for %s not yet implemented", expr.getClass().getSimpleName());
    }

    private Value evaluateMemberExpr(MemberExpr expr) {
        return Util.error("evaluation for %s not yet implemented", expr.getClass().getSimpleName());
    }
}
