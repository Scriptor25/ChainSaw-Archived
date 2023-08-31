package io.scriptor.chainsaw.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.scriptor.chainsaw.ast.*;
import io.scriptor.chainsaw.ast.expr.*;
import io.scriptor.chainsaw.ast.stmt.*;
import io.scriptor.chainsaw.runtime.function.FuncBody;
import io.scriptor.chainsaw.runtime.function.FuncParam;
import io.scriptor.chainsaw.runtime.function.Function;
import io.scriptor.chainsaw.runtime.function.NativeFuncBody;
import io.scriptor.chainsaw.runtime.function.RuntimeFuncBody;
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

        // System.out.println(GSON.toJson(environment));

        // get main entry point
        var main = Function.get(
                environment,
                FuncType.get(
                        environment,
                        NumberType.get(environment),
                        null,
                        false),
                "main", false, null);

        var value = evaluateFuncBody(main, null, null);

        System.out.printf("main -> %s%n", value);
    }

    private Value evaluateFuncBody(Function func, List<Value> args, Value my) {
        FuncBody body = func.getImpl();
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

                if (func.isConstructor())
                    environment.createValue("my", func.getType().getResult().emptyValue());

                if (func.getMemberOf() != null)
                    environment.createValue("my", my);
            }

            Value value = null;
            for (var stmt : ((RuntimeFuncBody) body).getStmts()) {
                value = evaluateStmt(stmt);
                if (RetValue.forMe(environment, value))
                    break;
                else
                    value = null;
            }

            if (func.isConstructor()) {
                if (RetValue.forMe(environment, value))
                    return Util.error("a constructor must not return anything!");

                value = environment.getValue("my");
            }

            environment = environment.getParent();

            return value;
        }

        if (body instanceof NativeFuncBody) {
            Map<String, Object> arguments = new HashMap<>();

            if (args != null) {
                var params = ((FuncType) body.getFunction().getType()).getParams();

                int arg = 0;
                for (var param : params)
                    arguments.put(param.id, Value.extract(args.get(arg++)).getValue());

                int startArg = arg;
                for (; arg < args.size(); arg++)
                    arguments.put("vararg" + (arg - startArg), Value.extract(args.get(arg)).getValue());
            }

            var member = Value.extract(my);
            return ((NativeFuncBody) body).run(environment, member != null ? member.getValue() : null, arguments);
        }

        return null;
    }

    private Value evaluateStmt(Stmt stmt) {
        if (stmt instanceof BodyStmt)
            return evaluateBodyStmt((BodyStmt) stmt);
        if (stmt instanceof ForStmt)
            return evaluateForStmt((ForStmt) stmt);
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
        environment = new Environment(environment);

        Value value = null;
        for (var s : stmt.stmts) {
            value = evaluateStmt(s);
            if (RetValue.forMe(environment, value))
                break;
            else
                value = null;
        }

        environment = environment.getParent();

        return value;
    }

    private Value evaluateForStmt(ForStmt stmt) {
        evaluateStmt(stmt.before);

        for (; ((NumberValue) Value.extract(evaluateExpr(stmt.condition))).getBool(); evaluateStmt(stmt.loop)) {
            var value = evaluateStmt(stmt.body);
            if (value instanceof RetValue)
                return value;
        }

        return null;
    }

    private Value evaluateFuncStmt(FuncStmt stmt) {
        List<FuncParam> params = new Vector<>();
        for (var p : stmt.params)
            params.add(new FuncParam(p.ident, Type.parseType(environment, p.type)));

        var result = Type.parseType(environment, stmt.constructor ? stmt.ident : stmt.type);
        var type = FuncType.get(environment, result, params, stmt.vararg);
        var func = Function.get(
                environment,
                type,
                stmt.ident,
                stmt.constructor,
                Type.parseType(environment, stmt.memberOf));

        if (stmt.impl != null)
            func.setImpl(new RuntimeFuncBody(func, stmt.impl.stmts));

        return null;
    }

    private Value evaluateIfStmt(IfStmt stmt) {
        var condition = Value.extract(evaluateExpr(stmt.condition));

        if (((NumberValue) condition).getBool())
            return evaluateStmt(stmt.isTrue);
        else if (stmt.isFalse != null)
            return evaluateStmt(stmt.isFalse);

        return null;
    }

    private Value evaluateRetStmt(RetStmt stmt) {
        return new RetValue(environment, Value.extract(evaluateExpr(stmt.value)));
    }

    private Value evaluateThingStmt(ThingStmt stmt) {

        Map<String, Type> fields = null;
        if (stmt.params != null) {
            fields = new HashMap<>();
            for (var p : stmt.params)
                fields.put(p.ident, Type.parseType(environment, p.type));
        }

        ThingType.create(environment, stmt.ident, fields);

        return null;
    }

    private Value evaluateValStmt(ValStmt stmt) {
        var type = Type.parseType(environment, stmt.type);

        Value value = null;
        if (stmt.value != null)
            value = Value.extract(evaluateExpr(stmt.value));

        if (value == null)
            value = type.emptyValue();

        environment.createValue(stmt.ident, value);

        return null;
    }

    private Value evaluateWhileStmt(WhileStmt stmt) {
        while (((NumberValue) Value.extract(evaluateExpr(stmt.condition))).getBool()) {
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
            return evaluateCallExpr((CallExpr) expr, null);
        if (expr instanceof CondExpr)
            return evaluateCondExpr((CondExpr) expr);
        if (expr instanceof ConstExpr)
            return evaluateConstExpr((ConstExpr) expr);
        if (expr instanceof IdentExpr)
            return evaluateIdentExpr((IdentExpr) expr);
        if (expr instanceof MemberExpr)
            return evaluateMemberExpr((MemberExpr) expr);
        if (expr instanceof UnaryExpr)
            return evaluateUnaryExpr((UnaryExpr) expr);

        return Util.error("not implemented: %s", expr);
    }

    private Value assignMember(MemberExpr expr, ThingValue vthing, Value value) {
        if (expr.member instanceof IdentExpr)
            return vthing.setField(((IdentExpr) expr.member).value, value);
        if (expr.member instanceof MemberExpr)
            return assignMember((MemberExpr) expr.member,
                    (ThingValue) vthing.getField(((MemberExpr) expr.member).thing), value);

        return Util.error("unknown error occurred");
    }

    private Value evaluateAssignExpr(AssignExpr expr) {
        var value = Value.extract(evaluateExpr(expr.value));

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
        var vleft = Value.extract(evaluateExpr(expr.left));
        var vright = Value.extract(evaluateExpr(expr.right));

        if (expr.operator.equals("=="))
            return new NumberValue(environment, vleft.equals(vright));

        var ltype = vleft.getType();
        var rtype = vright.getType();

        if (!ltype.isNumber() || !rtype.isNumber())
            return Util.error("number operations only available for numbers");

        var left = (NumberValue) vleft;
        var right = (NumberValue) vright;

        if (expr.operator.equals("||")) {
            return new NumberValue(environment, left.getBool() || right.getBool());
        }
        if (expr.operator.equals("&&")) {
            return new NumberValue(environment, left.getBool() && right.getBool());
        }

        if (expr.operator.equals("<")) {
            return new NumberValue(environment, left.getValue() < right.getValue());
        }
        if (expr.operator.equals(">")) {
            return new NumberValue(environment, left.getValue() > right.getValue());
        }
        if (expr.operator.equals("<=")) {
            return new NumberValue(environment, left.getValue() <= right.getValue());
        }
        if (expr.operator.equals(">=")) {
            return new NumberValue(environment, left.getValue() >= right.getValue());
        }

        if (expr.operator.equals("+")) {
            return new NumberValue(environment, left.getValue() + right.getValue());
        }
        if (expr.operator.equals("-")) {
            return new NumberValue(environment, left.getValue() - right.getValue());
        }
        if (expr.operator.equals("*")) {
            return new NumberValue(environment, left.getValue() * right.getValue());
        }
        if (expr.operator.equals("/")) {
            return new NumberValue(environment, left.getValue() / right.getValue());
        }

        return Util.error("binary operator '%s' not yet implemented", expr.operator);
    }

    private Value evaluateCallExpr(CallExpr expr, Value thing) {
        List<Value> args = new Vector<>();
        for (var arg : expr.args)
            args.add(Value.extract(evaluateExpr(arg)));

        var func = environment.getFunctionByRef(expr.function, args, thing == null ? null : thing.getType());

        if (func == null)
            return Util.error("undefined function id '%s', args %s, member of %s", expr.function, args,
                    thing == null ? null : thing.getType());

        return evaluateFuncBody(func, args, thing);
    }

    private Value evaluateCondExpr(CondExpr expr) {
        var condition = (NumberValue) Value.extract(evaluateExpr(expr.condition));

        if (condition.getBool())
            return evaluateExpr(expr.isTrue);

        return evaluateExpr(expr.isFalse);
    }

    private Value evaluateConstExpr(ConstExpr expr) {

        switch (expr.type) {
            case NUMBER:
                return new NumberValue(environment, expr.value);
            case STRING:
                return new StringValue(environment, expr.value);

            default:
                return Util.error("evaluation for %s.%s not yet implemented",
                        expr.getClass().getSimpleName(),
                        expr.type);
        }
    }

    private Value evaluateIdentExpr(IdentExpr expr) {
        return environment.getValue(expr.value);
    }

    private Value getThingMember(MemberExpr expr, ThingValue vthing) {
        if (expr.member instanceof IdentExpr)
            return vthing.getField(((IdentExpr) expr.member).value);
        if (expr.member instanceof CallExpr)
            return evaluateCallExpr((CallExpr) expr.member, vthing);
        if (expr.member instanceof MemberExpr) {
            var value = vthing.getField(((MemberExpr) expr.member).thing);
            if (value.getType().isThing())
                return getThingMember((MemberExpr) expr.member, (ThingValue) value);
            if (value.getType().isNative())
                return getNativeMember((MemberExpr) expr.member, (NativeValue<?>) value);
        }

        return Util.error("unknown error occurred");
    }

    private Value getNativeMember(MemberExpr expr, NativeValue<?> vnative) {
        if (expr.member instanceof CallExpr)
            return evaluateCallExpr((CallExpr) expr.member, vnative);

        return Util.error("unknown error occurred");
    }

    private Value evaluateMemberExpr(MemberExpr expr) {
        var value = environment.getValue(expr.thing);
        if (value.getType().isThing())
            return getThingMember(expr, (ThingValue) value);
        if (value.getType().isNative())
            return getNativeMember(expr, (NativeValue<?>) value);

        return Util.error("unknown error occurred");
    }

    private Value evaluateUnaryExpr(UnaryExpr expr) {
        var value = Value.extract(evaluateExpr(expr.expr));
        var type = value.getType();

        if (type.isNumber()) {
            var nval = (NumberValue) value;
            switch (expr.operator) {
                case "-":
                    return new NumberValue(environment, -nval.getValue());
                case "!":
                    return new NumberValue(environment, !nval.getBool());
            }
        }

        return Util.error("cannot use unary operator '%s' on value of type %s", expr.operator, type);
    }
}
