package chainsaw.runtime;

import java.io.File;
import java.util.*;

import chainsaw.Lexer;
import chainsaw.Parser;
import chainsaw.Util;
import chainsaw.ast.Program;
import chainsaw.ast.expr.*;
import chainsaw.ast.stmt.*;
import chainsaw.lang.StdList;
import chainsaw.runtime.function.*;
import chainsaw.runtime.type.NativeType;
import chainsaw.runtime.type.ThingType;
import chainsaw.runtime.type.Type;
import chainsaw.runtime.type.VoidType;
import chainsaw.runtime.value.*;

public class Interpreter {

    private Environment mEnv;
    private String mExecutionPath;

    public Interpreter(String executionPath) {
        mExecutionPath = executionPath;
        mEnv = new Environment(this);
    }

    public Interpreter(String executionPath, Environment env) {
        mExecutionPath = executionPath;
        mEnv = env;
    }

    public String getExecutionPath() {
        return mExecutionPath;
    }

    public Value evaluateProgram(Program program) {

        Value value = null;
        for (var stmt : program)
            value = evaluateStmt(stmt);

        return value;
    }

    public Value evaluateFunction(Value my, String name, Value... args) {
        var func = mEnv.getFunction(my, name, args);
        if (func == null)
            return Util.error("undefined function '%s', %s%s", name,
                    Arrays.toString(Value.toTypeArray(args)),
                    my != null ? " -> " + my.getType().toString() : "");

        if (func.isOpaque())
            return Util.error("function '%s', %s%s is opaque", name,
                    Arrays.toString(Value.toTypeArray(args)),
                    my != null ? " -> " + my.getType().toString() : "");

        mEnv = new Environment(mEnv);

        Value result = null;

        if (func.getImplementation() instanceof ASTImplementation) {

            var params = func.getParameters();
            int i = 0;
            for (; i < params.size(); i++)
                mEnv.createVariable(params.get(i).first, args[i]);

            var varargs = new NativeValue<>(mEnv, NativeType.get(mEnv, "list"), new StdList());
            for (; i < args.length; i++)
                varargs.getValue().add(args[i]);
            mEnv.createVariable("varargs", varargs);

            if (func.getSuperType() != null)
                mEnv.createVariable("my", my);

            if (func.isConstructor())
                mEnv.createVariable("my", func.getResultType().nullValue());

            var impl = (ASTImplementation) func.getImplementation();
            result = evaluateBodyStmt(impl.getBody());

            if (func.isConstructor()) {
                if (result != null && result.isReturn() && ((ReturnValue) result).isFrom(mEnv))
                    return Util.error("function '%s', %s is a constructor, so it should not return anything!");

                result = mEnv.getVariable("my");
            } else if (result != null && result.isReturn()) {
                if (((ReturnValue) result).isReceiver(mEnv) && !result.getType().equals(func.getResultType()))
                    return Util.error(
                            "function '%s', %s returns wrong data type: %s",
                            name,
                            Arrays.toString(Value.toTypeArray(args)),
                            result.getType());
            } else if (!func.getResultType().isVoid())
                return Util.error(
                        "return type of function '%s', %s is %s, but it doesn't return anything",
                        name,
                        Arrays.toString(Value.toTypeArray(args)),
                        func.getResultType());

        } else if (func.getImplementation() instanceof NativeImplementation) {
            var impl = (NativeImplementation) func.getImplementation();

            List<Object> arguments = new Vector<>();
            var params = func.getParameters();

            if (my != null && !(my instanceof NativeValue))
                arguments.add(my);

            int i = 0;
            for (; i < params.size(); i++)
                arguments.add(args[i]);

            if (func.isVarArg()) {
                Object[] varargs = new Object[args.length - i];
                for (int j = i; i < args.length; i++)
                    varargs[i - j] = args[i];

                arguments.add(varargs.length == 0 ? null : varargs);
            }

            result = impl.invoke(mEnv,
                    my == null
                            ? null
                            : my instanceof NativeValue ? ((NativeValue<?>) my).getValue() : null,
                    arguments.size() == 0
                            ? null
                            : arguments.toArray());
        }

        mEnv = mEnv.getParent();

        return Value.extract(result);
    }

    public Value evaluateStmt(Stmt stmt) {

        if (stmt == null)
            return null;

        if (stmt instanceof BodyStmt)
            return evaluateBodyStmt((BodyStmt) stmt);
        if (stmt instanceof ForStmt)
            return evaluateForStmt((ForStmt) stmt);
        if (stmt instanceof FunctionStmt)
            return evaluateFunctionStmt((FunctionStmt) stmt);
        if (stmt instanceof IfStmt)
            return evaluateIfStmt((IfStmt) stmt);
        if (stmt instanceof IncStmt)
            return evaluateIncStmt((IncStmt) stmt);
        if (stmt instanceof ReturnStmt)
            return evaluateReturnStmt((ReturnStmt) stmt);
        if (stmt instanceof SwitchStmt)
            return evaluateSwitchStmt((SwitchStmt) stmt);
        if (stmt instanceof ThingStmt)
            return evaluateThingStmt((ThingStmt) stmt);
        if (stmt instanceof VariableStmt)
            return evaluateVariableStmt((VariableStmt) stmt);
        if (stmt instanceof WhileStmt)
            return evaluateWhileStmt((WhileStmt) stmt);

        if (stmt instanceof Expr)
            return evaluateExpr((Expr) stmt);

        return Util.error("not yet implemented");
    }

    public Value evaluateBodyStmt(BodyStmt stmt) {

        mEnv = new Environment(mEnv);

        Value value = null;
        for (var s : stmt.statements) {
            value = evaluateStmt(s);
            if (value instanceof ReturnValue)
                break;
        }

        mEnv = mEnv.getParent();

        return value;
    }

    public Value evaluateForStmt(ForStmt stmt) {

        mEnv = new Environment(mEnv);

        Value value = null;
        for (evaluateStmt(stmt.entry); Value
                .asBoolean(Value.extract(evaluateExpr(stmt.condition))); evaluateStmt(stmt.next)) {
            value = evaluateStmt(stmt.body);
            if (value instanceof ReturnValue)
                break;
        }

        mEnv = mEnv.getParent();

        return value;
    }

    public Value evaluateFunctionStmt(FunctionStmt stmt) {

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
            return Util.error("failed to create function or set new function body for name '%s'", stmt.name);

        return null;
    }

    public Value evaluateIfStmt(IfStmt stmt) {

        mEnv = new Environment(mEnv);

        Value value = Value.asBoolean(Value.extract(evaluateExpr(stmt.condition)))
                ? evaluateStmt(stmt.thenStmt)
                : stmt.elseStmt == null ? null : evaluateStmt(stmt.elseStmt);

        mEnv = mEnv.getParent();

        return value;
    }

    public Value evaluateIncStmt(IncStmt stmt) {
        var file = new File(mExecutionPath, stmt.path);
        var source = Util.readFile(mExecutionPath == null
                ? stmt.path
                : file.getPath());

        var tokens = Lexer.tokenize(source);
        var parser = new Parser(tokens);
        var program = parser.parseProgram();

        var backupPath = mExecutionPath;
        mExecutionPath = file.getParent();
        var value = evaluateProgram(program);
        mExecutionPath = backupPath;

        return value;
    }

    public Value evaluateReturnStmt(ReturnStmt stmt) {
        if (stmt.value == null)
            return new ReturnValue(new VoidValue(mEnv, null), mEnv.getDepth());

        var value = Value.extract(evaluateExpr(stmt.value));
        return new ReturnValue(value, mEnv.getDepth());
    }

    public Value evaluateSwitchStmt(SwitchStmt stmt) {
        return Util.error("not yet implemented");
    }

    public Value evaluateThingStmt(ThingStmt stmt) {

        Map<String, Type> params = null;
        if (stmt.fields != null) {
            params = new HashMap<>();
            for (var param : stmt.fields)
                params.put(param.name, Type.parseType(mEnv, param.type));
        }

        ThingType.create(mEnv, stmt.name, params);

        return null;
    }

    public Value evaluateVariableStmt(VariableStmt stmt) {
        final var type = Type.parseType(mEnv, stmt.type);
        var value = stmt.value != null
                ? Value.extract(evaluateExpr(stmt.value))
                : type.nullValue();

        if (!value.getType().equals(type))
            value = Value.cast(mEnv, value, type);

        return mEnv.createVariable(
                stmt.name,
                value);
    }

    public Value evaluateWhileStmt(WhileStmt stmt) {

        mEnv = new Environment(mEnv);

        Value value = null;
        while (Value.asBoolean(Value.extract(evaluateExpr(stmt.condition)))) {
            value = evaluateStmt(stmt.body);
            if (value instanceof ReturnValue)
                break;
        }

        mEnv = mEnv.getParent();

        return value;
    }

    public Value evaluateExpr(Expr expr) {
        if (expr == null)
            return null;

        if (expr instanceof AssignmentExpr)
            return evaluateAssignmentExpr((AssignmentExpr) expr);
        if (expr instanceof BinaryExpr)
            return evaluateBinaryExpr((BinaryExpr) expr);
        if (expr instanceof CallExpr)
            return evaluateCallExpr((CallExpr) expr);
        if (expr instanceof ConditionExpr)
            return evaluateConditionExpr((ConditionExpr) expr);
        if (expr instanceof ConstantExpr)
            return evaluateConstantExpr((ConstantExpr) expr);
        if (expr instanceof IdentifierExpr)
            return evaluateIdentifierExpr((IdentifierExpr) expr);
        if (expr instanceof MemberExpr)
            return evaluateMemberExpr((MemberExpr) expr);
        if (expr instanceof UnaryExpr)
            return evaluateUnaryExpr((UnaryExpr) expr);

        return Util.error("not yet implemented");
    }

    public Value evaluateAssignmentExpr(AssignmentExpr expr) {
        if (expr.assignee instanceof IdentifierExpr)
            return mEnv.setVariable(((IdentifierExpr) expr.assignee).value, Value.extract(evaluateExpr(expr.value)));
        if (expr.assignee instanceof MemberExpr)
            return ((ThingValue) Value.extract(evaluateExpr(((MemberExpr) expr.assignee).thing)))
                    .setField(((IdentifierExpr) ((MemberExpr) expr.assignee).member).value,
                            Value.extract(evaluateExpr(expr.value)));

        return Util.error("not yet implemented");
    }

    public Value evaluateBinaryExpr(BinaryExpr expr) {

        var left = Value.extract(evaluateExpr(expr.left));
        var right = Value.extract(evaluateExpr(expr.right));

        switch (expr.operator) {
            case "&&":
                return new NumberValue(mEnv, Value.asBoolean(left) && Value.asBoolean(right) ? 1 : 0);
            case "||":
                return new NumberValue(mEnv, Value.asBoolean(left) || Value.asBoolean(right) ? 1 : 0);

            case "==":
                return new NumberValue(mEnv, Value.equals(mEnv, left, right) ? 1 : 0);
            case "!=":
                return new NumberValue(mEnv, Value.equals(mEnv, left, right) ? 0 : 1);
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

        return Util.error("operator '%s' is undefined for types '%s' and '%s'",
                expr.operator,
                left.getType(),
                right.getType());
    }

    public Value evaluateCallExpr(CallExpr expr) {
        Value[] args = new Value[expr.args.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = Value.extract(evaluateExpr(expr.args.get(i)));
        }

        if (expr.function instanceof IdentifierExpr)
            return evaluateFunction(null, ((IdentifierExpr) expr.function).value, args);
        if (expr.function instanceof MemberExpr)
            return evaluateFunction(Value.extract(evaluateExpr(((MemberExpr) expr.function).thing)),
                    ((IdentifierExpr) ((MemberExpr) expr.function).member).value, args);

        return Util.error("not yet implemented");
    }

    public Value evaluateConditionExpr(ConditionExpr expr) {
        return Value.extract(evaluateExpr(
                Value.asBoolean(Value.extract(evaluateExpr(expr.condition)))
                        ? expr.thenExpr
                        : expr.elseExpr));
    }

    public Value evaluateConstantExpr(ConstantExpr expr) {

        switch (expr.type) {
            case NUMBER:
                return new NumberValue(mEnv, Double.parseDouble(expr.value));
            case CHAR:
                return new CharValue(mEnv, expr.value.charAt(0));
            case STRING:
                return new StringValue(mEnv, expr.value);
        }

        return Util.error("undefined type '%s', value '%s'", expr.type, expr.value);
    }

    public Value evaluateIdentifierExpr(IdentifierExpr expr) {
        return mEnv.getVariable(expr.value);
    }

    public Value evaluateMemberExpr(MemberExpr expr) {
        return ((ThingValue) Value.extract(evaluateExpr(expr.thing))).getField(((IdentifierExpr) expr.member).value);
    }

    public Value evaluateUnaryExpr(UnaryExpr expr) {

        var value = Value.extract(evaluateExpr(expr.value));

        switch (expr.operator) {
            case "-":
                return Value.negate(mEnv, value);

            case "!":
                return Value.not(mEnv, value);
        }

        return Util.error("not yet implemented");
    }

}
