package io.scriptor.chainsaw.runtime;

import io.scriptor.chainsaw.ast.Program;
import io.scriptor.chainsaw.ast.expr.*;
import io.scriptor.chainsaw.ast.stmt.*;
import io.scriptor.chainsaw.runtime.function.*;
import io.scriptor.chainsaw.runtime.type.ThingType;
import io.scriptor.chainsaw.runtime.type.Type;
import io.scriptor.chainsaw.runtime.type.VoidType;
import io.scriptor.chainsaw.runtime.value.*;

import java.util.*;

public class Interpreter {

    private Environment mEnv = new Environment(this);

    public Value evaluate(Program program) {

        Value value = null;
        for (var stmt : program)
            value = evaluate(stmt);

        return value;
    }

    public Value evaluateFunction(Value thing, String name, Value... args) {
        var func = mEnv.getFunction(thing, name, args);
        if (func == null)
            return Error.error("undefined function '%s', %s%s", name,
                    Arrays.toString(Value.toTypeArray(args)),
                    thing != null ? " -> " + thing.getType().toString() : "");

        if (func.isOpaque())
            return Error.error("function '%s', %s%s is opaque", name,
                    Arrays.toString(Value.toTypeArray(args)),
                    thing != null ? " -> " + thing.getType().toString() : "");

        if (func.getImplementation() instanceof ASTImplementation) {
            var impl = (ASTImplementation) func.getImplementation();

            mEnv = new Environment(mEnv);

            var params = func.getParameters();
            int i = 0;
            for (; i < params.size(); i++)
                mEnv.createVariable(params.get(i).first, args[i]);

            for (int j = i; i < args.length; i++)
                mEnv.createVariable("vararg" + (i - j), args[i]);

            if (func.getSuperType() != null)
                mEnv.createVariable("my", thing);

            if (func.isConstructor())
                mEnv.createVariable("my", func.getResultType().nullValue());

            var result = evaluate(impl.getBody());

            if (func.isConstructor())
                result = new ReturnValue(mEnv.getVariable("my"), mEnv.getDepth());

            if (result instanceof ReturnValue) {
                var ret = (ReturnValue) result;
                if (ret.isReceiver(mEnv) && !ret.getType().equals(func.getResultType())) {
                    mEnv = mEnv.getParent();
                    return Error.error("function '%s', %s%s returns wrong data type: %s", name,
                            Arrays.toString(Value.toTypeArray(args)),
                            thing != null ? " -> " + thing.getType().toString() : "",
                            ret.getType());
                }
            } else if (!func.getResultType().isVoid()) {
                mEnv = mEnv.getParent();
                return Error.error("function '%s', %s%s is not void, but doesn't return anything", name,
                        Arrays.toString(Value.toTypeArray(args)),
                        thing != null ? " -> " + thing.getType().toString() : "");
            }

            mEnv = mEnv.getParent();

            return Value.extract(result);
        }

        if (func.getImplementation() instanceof NativeImplementation) {
            var impl = (NativeImplementation) func.getImplementation();

            mEnv = new Environment(mEnv);

            var params = func.getParameters();
            int i = 0;
            for (; i < params.size(); i++)
                mEnv.createVariable(params.get(i).first, args[i]);

            for (int j = i; i < args.length; i++)
                mEnv.createVariable("vararg" + (i - j), args[i]);

            if (func.getSuperType() != null)
                mEnv.createVariable("my", thing);

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
        if (stmt instanceof FunctionStmt)
            return evaluate((FunctionStmt) stmt);
        if (stmt instanceof IfStmt)
            return evaluate((IfStmt) stmt);
        if (stmt instanceof ReturnStmt)
            return evaluate((ReturnStmt) stmt);
        if (stmt instanceof SwitchStmt)
            return evaluate((SwitchStmt) stmt);
        if (stmt instanceof ThingStmt)
            return evaluate((ThingStmt) stmt);
        if (stmt instanceof VariableStmt)
            return evaluate((VariableStmt) stmt);
        if (stmt instanceof WhileStmt)
            return evaluate((WhileStmt) stmt);

        if (stmt instanceof Expr)
            return evaluate((Expr) stmt);

        return Error.error("not yet implemented");
    }

    public Value evaluate(BodyStmt stmt) {

        mEnv = new Environment(mEnv);

        Value value = null;
        for (var s : stmt.statements) {
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
        for (evaluate(stmt.entry); Value.asBoolean(Value.extract(evaluate(stmt.condition))); evaluate(stmt.next)) {
            value = evaluate(stmt.body);
            if (value instanceof ReturnValue)
                break;
        }

        mEnv = mEnv.getParent();

        return value;
    }

    public Value evaluate(FunctionStmt stmt) {

        List<Pair<String, Type>> params = new Vector<>();
        for (var param : stmt.parameters)
            params.add(new Pair<>(param.name, Type.parseType(mEnv, param.type)));

        FunctionImplementation impl = null;
        if (stmt.implementation != null)
            impl = new ASTImplementation(stmt.implementation);

        Type resultType = Type.parseType(mEnv, stmt.isConstructor ? stmt.name : stmt.resultType);
        var func = Function.get(
                mEnv,
                stmt.isConstructor,
                stmt.name,
                resultType == null ? VoidType.get(mEnv) : resultType,
                params,
                stmt.isVararg,
                Type.parseType(mEnv, stmt.superType),
                impl);

        if (func == null)
            return Error.error("failed to create function or set new function body for name '%s'", stmt.name);

        return null;
    }

    public Value evaluate(IfStmt stmt) {

        mEnv = new Environment(mEnv);

        Value value = Value.asBoolean(Value.extract(evaluate(stmt.condition)))
                ? evaluate(stmt.thenStmt)
                : stmt.elseStmt == null ? null : evaluate(stmt.elseStmt);

        mEnv = mEnv.getParent();

        return value;
    }

    public Value evaluate(ReturnStmt stmt) {
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
        if (stmt.fields != null) {
            params = new HashMap<>();
            for (var param : stmt.fields)
                params.put(param.name, Type.parseType(mEnv, param.type));
        }

        ThingType.create(mEnv, stmt.name, params);

        return null;
    }

    public Value evaluate(VariableStmt stmt) {
        return mEnv.createVariable(
                stmt.name,
                stmt.value != null
                        ? Value.extract(evaluate(stmt.value))
                        : Type.parseType(mEnv, stmt.type).nullValue());
    }

    public Value evaluate(WhileStmt stmt) {

        mEnv = new Environment(mEnv);

        Value value = null;
        while (Value.asBoolean(Value.extract(evaluate(stmt.condition)))) {
            value = evaluate(stmt.body);
            if (value instanceof ReturnValue)
                break;
        }

        mEnv = mEnv.getParent();

        return value;
    }

    public Value evaluate(Expr expr) {

        if (expr instanceof AssignmentExpr)
            return evaluate((AssignmentExpr) expr);
        if (expr instanceof BinaryExpr)
            return evaluate((BinaryExpr) expr);
        if (expr instanceof CallExpr)
            return evaluate((CallExpr) expr);
        if (expr instanceof ConditionExpr)
            return evaluate((ConditionExpr) expr);
        if (expr instanceof ConstantExpr)
            return evaluate((ConstantExpr) expr);
        if (expr instanceof IdentifierExpr)
            return evaluate((IdentifierExpr) expr);
        if (expr instanceof MemberExpr)
            return evaluate((MemberExpr) expr);
        if (expr instanceof UnaryExpr)
            return evaluate((UnaryExpr) expr);

        return Error.error("not yet implemented");
    }

    public Value evaluate(AssignmentExpr expr) {
        if (expr.assignee instanceof IdentifierExpr)
            return mEnv.setVariable(((IdentifierExpr) expr.assignee).value, Value.extract(evaluate(expr.value)));
        if (expr.assignee instanceof MemberExpr)
            return ((ThingValue) Value.extract(evaluate(((MemberExpr) expr.assignee).thing)))
                    .setField(((IdentifierExpr) ((MemberExpr) expr.assignee).member).value,
                            Value.extract(evaluate(expr.value)));

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
                return Value.add(mEnv, left, right, expr.assigning);
            case "-":
                return Value.sub(mEnv, left, right, expr.assigning);
            case "*":
                return Value.mul(mEnv, left, right, expr.assigning);
            case "/":
                return Value.div(mEnv, left, right, expr.assigning);
        }

        return Error.error("operator '%s' is undefined for types '%s' and '%s'",
                expr.operator,
                left.getType(),
                right.getType());
    }

    public Value evaluate(CallExpr expr) {
        Value[] args = new Value[expr.args.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = Value.extract(evaluate(expr.args.get(i)));
        }

        if (expr.function instanceof IdentifierExpr)
            return evaluateFunction(null, ((IdentifierExpr) expr.function).value, args);
        if (expr.function instanceof MemberExpr)
            return evaluateFunction(Value.extract(evaluate(((MemberExpr) expr.function).thing)),
                    ((IdentifierExpr) ((MemberExpr) expr.function).member).value, args);

        return Error.error("not yet implemented");
    }

    public Value evaluate(ConditionExpr expr) {
        return Value.extract(evaluate(
                Value.asBoolean(Value.extract(evaluate(expr.condition)))
                        ? expr.thenExpr
                        : expr.elseExpr));
    }

    public Value evaluate(ConstantExpr expr) {

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

    public Value evaluate(IdentifierExpr expr) {
        return mEnv.getVariable(expr.value);
    }

    public Value evaluate(MemberExpr expr) {
        return ((ThingValue) Value.extract(evaluate(expr.thing))).getField(((IdentifierExpr) expr.member).value);
    }

    public Value evaluate(UnaryExpr expr) {

        var value = Value.extract(evaluate(expr.value));

        switch (expr.operator) {
            case "-":
                return Value.negate(mEnv, value);

            case "!":
                return Value.not(mEnv, value);
        }

        return Error.error("not yet implemented");
    }

}
