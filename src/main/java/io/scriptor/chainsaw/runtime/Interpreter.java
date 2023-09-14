package io.scriptor.chainsaw.runtime;

import io.scriptor.chainsaw.ast.Program;
import io.scriptor.chainsaw.ast.expr.*;
import io.scriptor.chainsaw.ast.stmt.*;
import io.scriptor.chainsaw.runtime.function.*;
import io.scriptor.chainsaw.runtime.type.ThingType;
import io.scriptor.chainsaw.runtime.type.Type;
import io.scriptor.chainsaw.runtime.value.*;

import java.util.*;

public class Interpreter {

    private Environment mEnv = new Environment();

    public Value evaluate(Program program) {
        for (var stmt : program)
            evaluate(stmt);

        return null;
    }

    public Value evaluateFunction(Value member, String id, Value... args) {
        var func = mEnv.getFunction(member, id, args);
        if (func == null)
            return Error.error("undefined function '%s', %s%s", id,
                    Arrays.toString(Value.toTypeArray(args)),
                    member != null ? " -> " + member.getType().toString() : "");

        if (func.isOpaque())
            return Error.error("function '%s', %s%s is opaque", id,
                    Arrays.toString(Value.toTypeArray(args)),
                    member != null ? " -> " + member.getType().toString() : "");

        if (func.getImpl() instanceof ASTImpl) {
            var impl = (ASTImpl) func.getImpl();

            mEnv = new Environment(mEnv);

            var params = func.getParams();
            int i = 0;
            for (; i < params.size(); i++)
                mEnv.createVariable(params.get(i).first, args[i]);

            for (int j = i; i < args.length; i++)
                mEnv.createVariable("vararg" + (i - j), args[i]);

            if (func.getMember() != null)
                mEnv.createVariable("my", member);

            if (func.isConstructor())
                mEnv.createVariable("my", func.getResultType().nullValue());

            var result = evaluate(impl.getBody());

            if (func.isConstructor())
                result = new ReturnValue(mEnv.getVariable("my"), mEnv.getDepth());

            if (result instanceof ReturnValue) {
                var ret = (ReturnValue) result;
                if (ret.isReceiver(mEnv) && !ret.getType().equals(func.getResultType())) {
                    mEnv = mEnv.getParent();
                    return Error.error("function '%s', %s%s returns wrong data type: %s", id,
                            Arrays.toString(Value.toTypeArray(args)),
                            member != null ? " -> " + member.getType().toString() : "",
                            ret.getType());
                }
            } else if (!func.getResultType().isVoid()) {
                mEnv = mEnv.getParent();
                return Error.error("function '%s', %s%s is not void, but doesn't return anything", id,
                        Arrays.toString(Value.toTypeArray(args)),
                        member != null ? " -> " + member.getType().toString() : "");
            }

            mEnv = mEnv.getParent();

            return Value.extract(result);
        }

        if (func.getImpl() instanceof NativeImpl) {
            var impl = (NativeImpl) func.getImpl();

            mEnv = new Environment(mEnv);

            var params = func.getParams();
            int i = 0;
            for (; i < params.size(); i++)
                mEnv.createVariable(params.get(i).first, args[i]);

            for (int j = i; i < args.length; i++)
                mEnv.createVariable("vararg" + (i - j), args[i]);

            if (func.getMember() != null)
                mEnv.createVariable("my", member);

            if (func.isConstructor())
                mEnv.createVariable("my", func.getResultType().nullValue());

            var result = impl.invoke(mEnv);

            mEnv = mEnv.getParent();

            return Value.extract(result);
        }

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
        if (stmt instanceof VarStmt)
            return evaluate((VarStmt) stmt);
        if (stmt instanceof WhileStmt)
            return evaluate((WhileStmt) stmt);

        if (stmt instanceof Expr)
            return evaluate((Expr) stmt);

        return Error.error("evaluation not implemented: %s", stmt.getClass().getName());
    }

    public Value evaluate(BodyStmt stmt) {

        mEnv = new Environment(mEnv);

        Value value = null;
        for (var s : stmt.stmts) {
            value = evaluate(s);
            if (value instanceof ReturnValue)
                break;
        }

        mEnv = mEnv.getParent();

        return value;
    }

    public Value evaluate(ForStmt stmt) {

        mEnv = new Environment(mEnv);

        Value value = null;
        for (evaluate(stmt.before); Value.asBoolean(Value.extract(evaluate(stmt.condition))); evaluate(stmt.loop)) {
            value = evaluate(stmt.body);
            if (value instanceof ReturnValue)
                break;
        }

        mEnv = mEnv.getParent();

        return value;
    }

    public Value evaluate(FuncStmt stmt) {

        List<Pair<String, Type>> params = new Vector<>();
        for (var param : stmt.params)
            params.add(new Pair<>(param.ident, Type.parseType(mEnv, param.type)));

        FunctionImpl impl = null;
        if (stmt.impl != null)
            impl = new ASTImpl(stmt.impl);

        var func = Function.get(
                mEnv,
                stmt.constructor,
                stmt.ident,
                Type.parseType(mEnv, stmt.constructor ? stmt.ident : stmt.type),
                params,
                stmt.vararg,
                Type.parseType(mEnv, stmt.member),
                impl);

        if (func == null)
            return Error.error("failed to create or update function '%s'", stmt.ident);

        return null;
    }

    public Value evaluate(IfStmt stmt) {
        return Error.error("not yet implemented");
    }

    public Value evaluate(RetStmt stmt) {
        if (stmt.value == null)
            return new ReturnValue(new VoidValue(mEnv, null), mEnv.getDepth());

        var value = Value.extract(evaluate(stmt.value));
        return new ReturnValue(value, mEnv.getDepth());
    }

    public Value evaluate(SwitchStmt stmt) {
        return Error.error("not yet implemented");
    }

    public Value evaluate(ThingStmt stmt) {

        Map<String, Type> params = null;
        if (stmt.params != null) {
            params = new HashMap<>();
            for (var param : stmt.params)
                params.put(param.ident, Type.parseType(mEnv, param.type));
        }

        ThingType.create(mEnv, stmt.ident, params);

        return null;
    }

    public Value evaluate(VarStmt stmt) {
        return mEnv.createVariable(
                stmt.ident,
                stmt.value != null
                        ? Value.extract(evaluate(stmt.value))
                        : Type.parseType(mEnv, stmt.type).nullValue());
    }

    public Value evaluate(WhileStmt stmt) {
        return Error.error("not yet implemented");
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

        return Error.error("evaluation not implemented: %s", expr.getClass().getName());
    }

    public Value evaluate(AssignExpr expr) {

        if (expr.assigne instanceof IdentExpr)
            return mEnv.updateVariable(((IdentExpr) expr.assigne).value, Value.extract(evaluate(expr.value)));

        if (expr.assigne instanceof MemberExpr) {
            var assigne = (MemberExpr) expr.assigne;
            if (assigne.thing instanceof CallExpr) {
                var result = evaluate(assigne.thing);
                if (result.isThing())
                    if (assigne.member instanceof IdentExpr)
                        ((ThingValue) result).setField(((IdentExpr) assigne.member).value, evaluate(expr.value));
            }
        }

        return Error.error("not yet implemented");
    }

    public Value evaluate(BinaryExpr expr) {

        var left = Value.extract(evaluate(expr.left));
        var right = Value.extract(evaluate(expr.right));

        switch (expr.operator) {
            case "&&":
                return new NumberValue(mEnv, Value.asBoolean(left) && Value.asBoolean(right) ? 1 : 0);
            case "||":
                return new NumberValue(mEnv, Value.asBoolean(left) || Value.asBoolean(right) ? 1 : 0);

            case "==":
                return new NumberValue(mEnv, Value.equals(mEnv, left, right) ? 1 : 0);
            case "<":
                return new NumberValue(mEnv, Value.less(mEnv, left, right) ? 1 : 0);
            case ">":
                return new NumberValue(mEnv, Value.greater(mEnv, left, right) ? 1 : 0);
            case "<=":
                return new NumberValue(mEnv, Value.lessEq(mEnv, left, right) ? 1 : 0);
            case ">=":
                return new NumberValue(mEnv, Value.greaterEq(mEnv, left, right) ? 1 : 0);

            case "+":
                return Value.add(mEnv, left, right);
            case "-":
                return Value.sub(mEnv, left, right);
            case "*":
                return Value.mul(mEnv, left, right);
            case "/":
                return Value.div(mEnv, left, right);
        }

        return Error.error("operator '%s' is undefined for types '%s' and '%s'",
                expr.operator,
                left.getType(),
                right.getType());
    }

    public Value evaluate(CallExpr expr) {

        var args = new Value[expr.args.size()];
        for (int i = 0; i < args.length; i++)
            args[i] = Value.extract(evaluate(expr.args.get(i)));

        if (expr.function instanceof IdentExpr)
            return evaluateFunction(null, ((IdentExpr) expr.function).value, args);

        if (expr.function instanceof MemberExpr) {
            var mexpr = (MemberExpr) expr.function;
            if (mexpr.isHead())
                return evaluateFunction(evaluate(mexpr.thing), ((IdentExpr) mexpr.member).value, args);
        }

        return Error.error("not yet implemented");
    }

    public Value evaluate(CondExpr expr) {
        return Value.extract(evaluate(
                Value.asBoolean(Value.extract(evaluate(expr.condition)))
                        ? expr.isTrue
                        : expr.isFalse));
    }

    public Value evaluate(ConstExpr expr) {

        switch (expr.type) {
            case NUMBER:
                return new NumberValue(mEnv, Double.parseDouble(expr.value));
            case CHAR:
                return new CharValue(mEnv, expr.value.charAt(0));
            case STRING:
                return new StringValue(mEnv, expr.value);
        }

        return Error.error("undefined type '%s', value '%s'", expr.type, expr.value);
    }

    public Value evaluate(IdentExpr expr) {
        return mEnv.getVariable(expr.value);
    }

    public Value evaluate(MemberExpr expr) {

        String thing = null;
        if (expr.thing instanceof IdentExpr)
            thing = ((IdentExpr) expr.thing).value;

        String member = null;
        if (expr.member instanceof IdentExpr)
            member = ((IdentExpr) expr.member).value;

        if (thing == null || member == null)
            return Error.error("not yet implemented");

        ThingValue vthing = mEnv.getVariable(thing);
        return vthing.getField(member);
    }

    public Value evaluate(UnaryExpr expr) {
        return Error.error("not yet implemented");
    }

}
