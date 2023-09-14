package io.scriptor.chainsaw.runtime;

import io.scriptor.chainsaw.runtime.function.Function;
import io.scriptor.chainsaw.runtime.function.NativeImplementation;
import io.scriptor.chainsaw.runtime.function.Pair;
import io.scriptor.chainsaw.runtime.type.*;
import io.scriptor.chainsaw.runtime.value.NativeValue;
import io.scriptor.chainsaw.runtime.value.NumberValue;
import io.scriptor.chainsaw.runtime.value.StringValue;
import io.scriptor.chainsaw.runtime.value.Value;

import java.util.*;

public class Environment {

    private final Environment mParent;
    private final Interpreter mInterpreter;
    private final Map<String, Value> mVariables = new HashMap<>();
    private final Map<Class<? extends Type>, Type> mTypes = new HashMap<>();
    private final Map<String, ThingType> mThingTypes = new HashMap<>();
    private final Map<String, NativeType<?>> mNativeTypes = new HashMap<>();
    private final Map<String, List<Function>> mFunctions = new HashMap<>();

    public Environment(Interpreter interpreter) {
        this(null, interpreter);

        // Native Types
        NativeType.get(this, FileStream.class, "file");
        NativeType.get(this, List.class, "list");

        // Native Functions
        List<Pair<String, Type>> params = new Vector<>();
        params.add(new Pair<>("fmt", StringType.get(this)));
        Function.get(this, false, "out", VoidType.get(this), params, true, null, new NativeImplementation(
                env -> {
                    StringValue fmt = env.getVariable("fmt");
                    List<Object> args = new Vector<>();
                    for (int i = 0; env.hasVariable("vararg" + i); i++)
                        args.add(env.getVariable("vararg" + i));

                    System.out.printf(fmt.getValue(), args.toArray());

                    return null;
                }));

        Function.get(this, false, "inf", NumberType.get(this), Collections.emptyList(), false, null,
                new NativeImplementation(
                        env -> new NumberValue(env, Double.MAX_VALUE)));

        params = new Vector<>();
        params.add(new Pair<>("x", NumberType.get(this)));
        Function.get(this, false, "floor", NumberType.get(this), params, false, null, new NativeImplementation(
                env -> new NumberValue(env, Math.floor((double) env.getVariable("x").getValue()))));

        params = new Vector<>();
        params.add(new Pair<>("x", NumberType.get(this)));
        Function.get(this, false, "sqrt", NumberType.get(this), params, false, null, new NativeImplementation(
                env -> new NumberValue(env, Math.sqrt((double) env.getVariable("x").getValue()))));

        params = new Vector<>();
        params.add(new Pair<>("x", NumberType.get(this)));
        Function.get(this, false, "acos", NumberType.get(this), params, false, null, new NativeImplementation(
                env -> new NumberValue(env, Math.acos((double) env.getVariable("x").getValue()))));

        params = new Vector<>();
        params.add(new Pair<>("y", NumberType.get(this)));
        params.add(new Pair<>("x", NumberType.get(this)));
        Function.get(this, false, "atan2", NumberType.get(this), params, false, null, new NativeImplementation(
                env -> new NumberValue(env, Math.atan2((double) env.getVariable("y").getValue(),
                        (double) env.getVariable("x").getValue()))));

        // file
        params = new Vector<>();
        params.add(new Pair<>("name", StringType.get(this)));
        params.add(new Pair<>("mode", StringType.get(this)));
        Function.get(this, true, "file", VoidType.get(this), params, false, null,
                new NativeImplementation(
                        env -> new NativeValue<>(env, NativeType.get(env, FileStream.class, "file"),
                                new FileStream((String) env.getVariable("name").getValue(),
                                        (String) env.getVariable("mode").getValue()))));

        params = new Vector<>();
        params.add(new Pair<>("fmt", StringType.get(this)));
        Function.get(this, false, "out", VoidType.get(this), params, true,
                NativeType.get(this, FileStream.class, "file"), new NativeImplementation(env -> {
                    List<Object> args = new Vector<>();
                    for (int i = 0; env.hasVariable("vararg" + i); i++)
                        args.add(env.getVariable("vararg" + i));

                    ((FileStream) env.getVariable("my").getValue())
                            .out(String.format((String) env.getVariable("fmt").getValue(), args.toArray()));

                    return null;
                }));

        Function.get(this, false, "close", VoidType.get(this), Collections.emptyList(), false,
                NativeType.get(this, FileStream.class, "file"), new NativeImplementation(env -> {
                    ((FileStream) env.getVariable("my").getValue()).close();
                    return null;
                }));

        // list
        Function.get(this, true, "list", VoidType.get(this), Collections.emptyList(), false, null,
                new NativeImplementation(
                        env -> new NativeValue<>(env, NativeType.get(env, List.class, "list"), new Vector<Value>())));

        params = new Vector<>();
        params.add(new Pair<>("object", VoidType.get(this)));
        Function.get(this, false, "add", VoidType.get(this), params, false, NativeType.get(this, List.class, "list"),
                new NativeImplementation(env -> {
                    ((List<?>) env.getVariable("my").getValue()).add(env.getVariable("object"));
                    return null;
                }));

        Function.get(this, false, "size", VoidType.get(this), Collections.emptyList(), false,
                NativeType.get(this, List.class, "list"),
                new NativeImplementation(
                        env -> new NumberValue(env, ((List<?>) env.getVariable("my").getValue()).size())));

        params = new Vector<>();
        params.add(new Pair<>("index", NumberType.get(this)));
        Function.get(this, false, "get", VoidType.get(this), params, false, NativeType.get(this, List.class, "list"),
                new NativeImplementation(env -> (Value) ((List<?>) env.getVariable("my").getValue())
                        .get((int) (double) env.getVariable("index").getValue())));
    }

    public Environment(Environment parent, Interpreter interpreter) {
        mParent = parent;
        mInterpreter = interpreter;
    }

    public Environment(Environment parent) {
        this(parent, null);
    }

    public Environment getParent() {
        return mParent;
    }

    public Interpreter getInterpreter() {
        return mInterpreter == null ? mParent == null ? null : mParent.getInterpreter() : mInterpreter;
    }

    public int getDepth() {
        if (mParent == null)
            return 0;
        return 1 + mParent.getDepth();
    }

    public boolean hasVariable(String name) {
        if (!mVariables.containsKey(name))
            if (mParent != null)
                return mParent.hasVariable(name);
            else
                return false;

        return true;
    }

    @SuppressWarnings("unchecked")
    public <V extends Value> V getVariable(String name) {
        if (!mVariables.containsKey(name))
            if (mParent != null)
                return mParent.getVariable(name);
            else
                return Error.error("undefined variable '%s'", name);

        return (V) mVariables.get(name);
    }

    public <V extends Value> V createVariable(String name, V value) {
        if (mVariables.containsKey(name))
            return Error.error("variable '%s' already defined", name);

        mVariables.put(name, value);
        return value;
    }

    public <V extends Value> V setVariable(String name, V value) {
        if (!mVariables.containsKey(name))
            if (mParent != null)
                return mParent.setVariable(name, value);
            else
                return Error.error("undefined variable '%s'", name);

        mVariables.put(name, value);
        return value;
    }

    public <T extends Type> T getType(Class<T> type) {
        if (!mTypes.containsKey(type))
            if (mParent != null)
                return mParent.getType(type);

        return type.cast(mTypes.get(type));
    }

    public <T extends Type> T addType(T type) {
        if (mTypes.containsKey(type.getClass()))
            return null;

        mTypes.put(type.getClass(), type);
        return type;
    }

    public ThingType getThingType(String name) {
        if (!mThingTypes.containsKey(name))
            if (mParent != null)
                return mParent.getThingType(name);

        return mThingTypes.get(name);
    }

    public ThingType addThingType(ThingType type) {
        if (mThingTypes.containsKey(type.getId()))
            return null;

        mThingTypes.put(type.getId(), type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public <C> NativeType<C> getNativeType(Class<C> nativeClass, String alias) {
        if (alias == null) {
            for (var type : mNativeTypes.values())
                if (type.getNativeClass().equals(nativeClass))
                    return (NativeType<C>) type;

            if (mParent != null)
                return mParent.getNativeType(nativeClass, alias);

            return null;
        }

        if (!mNativeTypes.containsKey(alias))
            if (mParent != null)
                return mParent.getNativeType(nativeClass, alias);

        return (NativeType<C>) mNativeTypes.get(alias);
    }

    public <C> NativeType<C> addNativeType(NativeType<C> type) {
        if (mNativeTypes.containsKey(type.getAlias()))
            return null;

        mNativeTypes.put(type.getAlias(), type);
        return type;
    }

    public Function getFunction(Value thing, String name, Value... args) {
        if (!mFunctions.containsKey(name))
            if (mParent != null)
                return mParent.getFunction(thing, name, args);

        var functions = mFunctions.computeIfAbsent(name, funid -> new Vector<>());
        for (var func : functions) {

            if ((thing == null && func.getSuperType() != null) || (thing != null && func.getSuperType() == null))
                continue;
            if (!(thing == null && func.getSuperType() == null) && !func.getSuperType().equals(thing.getType()))
                continue;

            var params = func.getParameters();
            if (!func.isVarArg() && params.size() != args.length)
                continue;

            int i = 0;
            for (; i < params.size(); i++)
                if (!params.get(i).second.equals(args[i].getType()))
                    break;

            if (i < params.size())
                continue;

            return func;
        }

        return null;
    }

    public Function getFunction(String name, List<Pair<String, Type>> parameters, boolean isVararg, Type superType) {
        if (!mFunctions.containsKey(name))
            if (mParent != null)
                return mParent.getFunction(name, parameters, isVararg, superType);

        var functions = mFunctions.computeIfAbsent(name, funid -> new Vector<>());
        for (var func : functions) {

            if (!func.isSimilar(name, parameters, isVararg, superType))
                continue;

            return func;
        }

        return null;
    }

    public Function addFunction(Function function) {
        var functions = mFunctions.computeIfAbsent(function.getName(), id -> new Vector<>());
        if (functions.contains(function))
            return null;

        functions.add(function);
        return function;
    }

}
