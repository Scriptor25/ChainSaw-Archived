package io.scriptor.chainsaw.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.scriptor.chainsaw.ast.*;
import io.scriptor.chainsaw.ast.expr.*;
import io.scriptor.chainsaw.ast.stmt.*;
import io.scriptor.chainsaw.runtime.type.*;
import io.scriptor.chainsaw.runtime.value.*;

public class Interpreter {

    private Program program;
    private Environment environment = new Environment();

    public Interpreter(Program program) {
        this.program = program;
    }

    public void evaluate() {
        environment.reset();

        for (var stmt : program)
            evaluateStmt(stmt);

        // System.out.println(environment);

        Map<String, Type> params = new HashMap<>();
        params.put("msg", ThingType.get(environment, "string"));

        var func = Function.get(environment,
                FuncType.get(
                        environment,
                        VoidType.get(environment),
                        params,
                        true),
                "out");

        func.setImpl(new NativeFuncBody(func, args -> {
            String msg = ((StringValue) args.get(0)).getValue();

            Object[] obj = new Object[args.size() - 1];
            for (int i = 0; i < obj.length; i++)
                obj[i] = args.get(i + 1);

            System.out.printf(msg, obj);

            return null;
        }));

        var main = Function.get(
                environment,
                FuncType.get(
                        environment,
                        IntType.get(environment, 32),
                        null,
                        false),
                "main");

        evaluateFuncBody(main.getImpl(), null);
    }

    private Type evaluateType(String type) {
        if (type == null)
            return VoidType.get(environment);

        switch (type) {
            case "int":
                return IntType.get(environment, 32);
            case "float":
                return FloatType.get(environment, 32);
        }

        var t = ThingType.get(environment, type);
        if (t == null)
            return Util.error("undefined Type '%s'", type);

        return t;
    }

    private Value evaluateFuncBody(FuncBody body, List<Value> args) {
        if (body instanceof RuntimeFuncBody) {
            environment = new Environment(environment);

            if (args != null) {
                var params = ((FuncType) body.getFunction().getType()).getParams();

                int arg = 0;
                for (var param : params.keySet())
                    environment.createValue(param, args.get(arg++));

                int startArg = arg;
                for (; arg < args.size(); arg++)
                    environment.createValue("vararg" + (arg - startArg), args.get(arg));
            }

            for (var stmt : ((RuntimeFuncBody) body).getStmts()) {
                var value = evaluateStmt(stmt);
                if (value instanceof RetValue)
                    return value;
            }

            environment = environment.getParent();

            return null;
        }

        if (body instanceof NativeFuncBody)
            return ((NativeFuncBody) body).run(args);

        return null;
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

        if (stmt == null)
            return Util.error("cannot evaluate null BodyStmt");

        for (var s : stmt.stmts) {
            var value = evaluateStmt(s);
            if (value instanceof RetValue)
                return value;
        }

        return null;
    }

    private Value evaluateFuncStmt(FuncStmt stmt) {

        Map<String, Type> params = new HashMap<>();
        for (var p : stmt.params)
            params.put(p.ident, evaluateType(p.type));

        var result = evaluateType(stmt.type);
        var type = FuncType.get(environment, result, params, stmt.vararg);
        var func = Function.get(environment, type, stmt.ident);

        if (stmt.impl != null)
            func.setImpl(new RuntimeFuncBody(func, stmt.impl.stmts));

        return null;
    }

    private Value evaluateIfStmt(IfStmt stmt) {
        var condition = evaluateExpr(stmt.condition);

        if (((IntValue) condition).getBooleanValue())
            return evaluateStmt(stmt.isTrue);
        else if (stmt.isFalse != null)
            return evaluateStmt(stmt.isFalse);

        return null;
    }

    private Value evaluateRetStmt(RetStmt stmt) {
        return new RetValue(environment, evaluateExpr(stmt.value));
    }

    private Value evaluateThingStmt(ThingStmt stmt) {

        Map<String, Type> fields = null;
        if (stmt.params != null) {
            fields = new HashMap<>();
            for (var p : stmt.params)
                fields.put(p.ident, evaluateType(p.type));
        }

        ThingType.create(environment, stmt.ident, fields);

        return null;
    }

    private Value evaluateValStmt(ValStmt stmt) {
        return Util.error("evaluation for %s not yet implemented", stmt.getClass().getSimpleName());
    }

    private Value evaluateExpr(Expr expr) {

        if (expr instanceof AssignExpr)
            return evaluateAssignExpr((AssignExpr) expr);
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

    private Value evaluateAssignExpr(AssignExpr expr) {
        String id = null;
        if (expr.assigne instanceof IdentExpr)
            id = ((IdentExpr) expr.assigne).value;
        else
            return Util.error("assigning to %s not yet implemented", expr.assigne);

        var value = evaluateExpr(expr.value);

        return environment.setValue(id, value);
    }

    private Value evaluateBinaryExpr(BinaryExpr expr) {
        var left = evaluateExpr(expr.left);
        var right = evaluateExpr(expr.right);

        var ltype = left.getType();
        var rtype = right.getType();

        if((ltype.isInt() || ltype.isFloat()) && (rtype.isInt() || rtype.isFloat())){
            
        }

        Value value = null;
        switch (expr.operator) {
            case "==":
                return new IntValue(environment, IntType.get(environment, 1), left.equals(right));

            case "-":
                return Value.get(environment, Type.getBest(ltype, rtype), null);
        }

        if (value == null)
            return Util.error("binary operator '%s' not yet implemented", expr.operator);

        return ;
    }

    private Value evaluateCallExpr(CallExpr expr) {
        List<Value> args = new Vector<>();
        for (var arg : expr.args)
            args.add(evaluateExpr(arg));

        var func = environment.getFunctionByRef(expr.function, args);

        return evaluateFuncBody(func.getImpl(), args);
    }

    private Value evaluateConstExpr(ConstExpr expr) {

        switch (expr.type) {
            case INT:
                return new IntValue(environment, IntType.get(environment, 32), expr.value);
            case FLOAT:
                return new FloatValue(environment, FloatType.get(environment, 32), expr.value);
            case STRING:
                return new StringValue(environment, ThingType.get(environment, "string"), expr.value);

            default:
                return Util.error("evaluation for %s.%s not yet implemented",
                        expr.getClass().getSimpleName(),
                        expr.type);
        }
    }

    private Value evaluateIdentExpr(IdentExpr expr) {
        return environment.getValue(expr.value);
    }

    private Value evaluateMemberExpr(MemberExpr expr) {
        return Util.error("evaluation for %s not yet implemented", expr.getClass().getSimpleName());
    }
}
