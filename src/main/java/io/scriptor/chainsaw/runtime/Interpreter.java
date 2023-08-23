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

        { // out function
            ThingType.create(environment, "string", null);

            List<FuncParam> params = new Vector<>();
            params.add(new FuncParam("msg", ThingType.get(environment, "string")));

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
                for (int i = 0; i < obj.length; i++) {
                    obj[i] = args.get(i + 1).getValue();
                    if (obj[i] instanceof Value)
                        obj[i] = ((Value) obj[i]).getValue();
                }

                System.out.printf(msg, obj);

                return null;
            }));
        }

        // get main entry point
        var main = Function.get(
                environment,
                FuncType.get(
                        environment,
                        NumberType.get(environment, 32),
                        null,
                        false),
                "main");

        var value = evaluateFuncBody(main.getImpl(), null);
        System.out.printf("program returned %s%n", value);
    }

    private Type evaluateType(String type) {
        if (type == null)
            return VoidType.get(environment);

        switch (type) {
            case "num":
                return NumberType.get(environment, 32);
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
                for (var param : params)
                    environment.createValue(param.id, args.get(arg++));

                int startArg = arg;
                for (; arg < args.size(); arg++)
                    environment.createValue("vararg" + (arg - startArg), args.get(arg));
            }

            Value value = null;
            for (var stmt : ((RuntimeFuncBody) body).getStmts()) {
                value = evaluateStmt(stmt);
                if (value instanceof RetValue)
                    break;
            }

            environment = environment.getParent();

            return value;
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
        if (stmt instanceof WhileStmt)
            return evaluateWhileStmt((WhileStmt) stmt);

        return evaluateExpr((Expr) stmt);
    }

    private Value evaluateBodyStmt(BodyStmt stmt) {

        if (stmt == null)
            return Util.error("cannot evaluate null BodyStmt");

        environment = new Environment(environment);

        Value value = null;
        for (var s : stmt.stmts) {
            value = evaluateStmt(s);
            if (value instanceof RetValue)
                break;
        }

        environment = environment.getParent();

        return value;
    }

    private Value evaluateFuncStmt(FuncStmt stmt) {

        List<FuncParam> params = new Vector<>();
        for (var p : stmt.params)
            params.add(new FuncParam(p.ident, evaluateType(p.type)));

        var result = evaluateType(stmt.type);
        var type = FuncType.get(environment, result, params, stmt.vararg);
        var func = Function.get(environment, type, stmt.ident);

        if (stmt.impl != null)
            func.setImpl(new RuntimeFuncBody(func, stmt.impl.stmts));

        return null;
    }

    private Value evaluateIfStmt(IfStmt stmt) {
        var condition = evaluateExpr(stmt.condition);

        if (((NumberValue) condition).getBool())
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
        environment.createValue(stmt.ident, Value.get(environment, evaluateType(stmt.type)));

        if (stmt.value != null)
            environment.setValue(stmt.ident, evaluateExpr(stmt.value).extract());

        return null;
    }

    private Value evaluateWhileStmt(WhileStmt stmt) {
        while (((NumberValue) evaluateExpr(stmt.condition)).getBool()) {
            var value = evaluateStmt(stmt.body);
            if (value instanceof RetValue)
                return value;
        }

        return null;
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

    private Value assignMember(MemberExpr expr, ThingValue vthing, Value value) {
        if (expr.member instanceof IdentExpr)
            return vthing.setField(((IdentExpr) expr.member).value, value);
        if (expr.member instanceof MemberExpr)
            return assignMember((MemberExpr) expr.member,
                    (ThingValue) vthing.getField(((MemberExpr) expr.member).thing), value);

        return Util.error("");
    }

    private Value evaluateAssignExpr(AssignExpr expr) {
        var value = evaluateExpr(expr.value);
        value = value.extract();

        if (expr.assigne instanceof IdentExpr) {
            var id = ((IdentExpr) expr.assigne).value;
            return environment.setValue(id, value);
        }

        if (expr.assigne instanceof MemberExpr) {
            var thing = ((MemberExpr) expr.assigne).thing;
            var vthing = (ThingValue) environment.getValue(thing);
            return assignMember((MemberExpr) expr.assigne, vthing, value);
        }

        return Util.error("assigning to %s not yet implemented", expr.assigne);

    }

    private Value evaluateBinaryExpr(BinaryExpr expr) {
        var left = evaluateExpr(expr.left);
        var right = evaluateExpr(expr.right);

        left = left.extract();
        right = right.extract();

        if (expr.operator.equals("=="))
            return new NumberValue(environment, NumberType.get(environment, 1), left.equals(right));

        var ltype = left.getType();
        var rtype = right.getType();

        if (!ltype.isNumber() || !rtype.isNumber())
            return Util.error("number operations only available for numbers");

        var btype = Type.getBest(ltype, rtype);
        left = Value.cast(left, btype);
        right = Value.cast(right, btype);

        if (expr.operator.equals("<")) {
            return new NumberValue(environment, NumberType.get(environment, 1),
                    ((NumberValue) left).getValue().compareTo(((NumberValue) right).getValue()) == -1);
        }
        if (expr.operator.equals(">")) {
            return new NumberValue(environment, NumberType.get(environment, 1),
                    ((NumberValue) left).getValue().compareTo(((NumberValue) right).getValue()) == 1);
        }
        if (expr.operator.equals("<=")) {
            return new NumberValue(environment, NumberType.get(environment, 1),
                    ((NumberValue) left).getValue().compareTo(((NumberValue) right).getValue()) != 1);
        }
        if (expr.operator.equals(">=")) {
            return new NumberValue(environment, NumberType.get(environment, 1),
                    ((NumberValue) left).getValue().compareTo(((NumberValue) right).getValue()) != -1);
        }

        boolean assign = false;
        if (expr.operator.length() > 1 && expr.operator.charAt(1) == '=') {
            assign = true;
            expr.operator = expr.operator.substring(0, 1);
        }

        Value value = null;
        if (expr.operator.equals("+")) {
            value = new NumberValue(environment, (NumberType) btype,
                    ((NumberValue) left).getValue().add(((NumberValue) right).getValue()));
        } else if (expr.operator.equals("-")) {
            value = new NumberValue(environment, (NumberType) btype,
                    ((NumberValue) left).getValue().subtract(((NumberValue) right).getValue()));
        } else if (expr.operator.equals("*")) {
            value = new NumberValue(environment, (NumberType) btype,
                    ((NumberValue) left).getValue().multiply(((NumberValue) right).getValue()));
        } else if (expr.operator.equals("/")) {
            value = new NumberValue(environment, (NumberType) btype,
                    ((NumberValue) left).getValue().divide(((NumberValue) right).getValue()));
        }

        if (value != null) {
            if (!assign)
                return value;

            if (expr.left instanceof IdentExpr) {
                var id = ((IdentExpr) expr.left).value;
                return environment.setValue(id, value);
            }

            if (expr.left instanceof MemberExpr) {
                var thing = ((MemberExpr) expr.left).thing;
                var vthing = (ThingValue) environment.getValue(thing);
                return assignMember((MemberExpr) expr.left, vthing, value);
            }
        }

        return Util.error("binary operator '%s' not yet implemented",
                expr.operator);
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
            case NUMBER:
                return new NumberValue(environment, NumberType.get(environment, 32), expr.value);
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

    private Value getMember(MemberExpr expr, ThingValue vthing) {
        if (expr.member instanceof IdentExpr)
            return vthing.getField(((IdentExpr) expr.member).value);
        if (expr.member instanceof MemberExpr)
            return getMember((MemberExpr) expr.member,
                    (ThingValue) vthing.getField(((MemberExpr) expr.member).thing));

        return Util.error("");
    }

    private Value evaluateMemberExpr(MemberExpr expr) {
        var vthing = (ThingValue) environment.getValue(expr.thing);
        return getMember(expr, vthing);
    }
}
