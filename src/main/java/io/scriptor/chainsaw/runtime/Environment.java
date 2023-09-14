package io.scriptor.chainsaw.runtime;

import io.scriptor.chainsaw.runtime.function.Function;
import io.scriptor.chainsaw.runtime.function.NativeImpl;
import io.scriptor.chainsaw.runtime.function.Pair;
import io.scriptor.chainsaw.runtime.type.*;
import io.scriptor.chainsaw.runtime.value.NumberValue;
import io.scriptor.chainsaw.runtime.value.StringValue;
import io.scriptor.chainsaw.runtime.value.Value;

import java.util.*;

public class Environment {

    private final Environment mParent;
    private final Map<String, Value> mVariables = new HashMap<>();
    private final Map<Class<? extends Type>, Type> mTypes = new HashMap<>();
    private final Map<String, ThingType> mThingTypes = new HashMap<>();
    private final Map<String, NativeType<?>> mNativeTypes = new HashMap<>();
    private final Map<String, List<Function>> mFunctions = new HashMap<>();

    public Environment() {
        this(null);
    }

    public Environment(Environment parent) {
        mParent = parent;

        // Native Types
        NativeType.get(this, Object.class, "file");

        // Native Functions
        List<Pair<String, Type>> params = new Vector<>();
        params.add(new Pair<>("fmt", StringType.get(this)));
        Function.get(this, false, "out", VoidType.get(this), params, true, null, new NativeImpl(
                env -> {
                    StringValue fmt = env.getVariable("fmt");
                    List<Object> args = new Vector<>();
                    for (int i = 0; env.hasVariable("vararg" + i); i++)
                        args.add(env.getVariable("vararg" + i));

                    System.out.printf(fmt.getValue(), args.toArray());

                    return null;
                }));

        Function.get(this, false, "inf", NumberType.get(this), Collections.emptyList(), false, null, new NativeImpl(
                env -> new NumberValue(env, Double.MAX_VALUE)));

        params = new Vector<>();
        params.add(new Pair<>("x", NumberType.get(this)));
        Function.get(this, false, "floor", NumberType.get(this), params, false, null, new NativeImpl(
                env -> new NumberValue(env, Math.floor((double) env.getVariable("x").getValue()))));
    }

    public Environment getParent() {
        return mParent;
    }

    public int getDepth() {
        if (mParent == null)
            return 0;
        return 1 + mParent.getDepth();
    }

    public boolean hasVariable(String id) {
        if (!mVariables.containsKey(id))
            if (mParent != null)
                return mParent.hasVariable(id);
            else
                return false;

        return true;
    }

    @SuppressWarnings("unchecked")
    public <V extends Value> V getVariable(String id) {
        if (!mVariables.containsKey(id))
            if (mParent != null)
                return mParent.getVariable(id);
            else
                return Error.error("undefined variable '%s'", id);

        return (V) mVariables.get(id);
    }

    public <V extends Value> V createVariable(String id, V value) {
        if (mVariables.containsKey(id))
            return Error.error("variable '%s' already defined", id);

        mVariables.put(id, value);
        return value;
    }

    public <V extends Value> V updateVariable(String id, V value) {
        if (!mVariables.containsKey(id))
            return Error.error("undefined variable '%s'", id);

        mVariables.put(id, value);
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

    public ThingType getThingType(String id) {
        if (!mThingTypes.containsKey(id))
            if (mParent != null)
                return mParent.getThingType(id);

        return mThingTypes.get(id);
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

    public Function getFunction(Value member, String id, Value... args) {
        if (!mFunctions.containsKey(id))
            if (mParent != null)
                return mParent.getFunction(member, id, args);

        var functions = mFunctions.computeIfAbsent(id, funid -> new Vector<>());
        for (var func : functions) {

            if ((member == null && func.getMember() != null) || (member != null && func.getMember() == null))
                continue;
            if (!(member == null && func.getMember() == null) && !func.getMember().equals(member.getType()))
                continue;

            var params = func.getParams();
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

    public Function getFunction(String id, List<Pair<String, Type>> params, boolean vararg, Type member) {
        if (!mFunctions.containsKey(id))
            if (mParent != null)
                return mParent.getFunction(id, params, vararg, member);

        var functions = mFunctions.computeIfAbsent(id, funid -> new Vector<>());
        for (var func : functions) {

            if (!func.similar(id, params, vararg, member))
                continue;

            return func;
        }

        return null;
    }

    public Function addFunction(Function func) {
        var functions = mFunctions.computeIfAbsent(func.getId(), id -> new Vector<>());
        if (functions.contains(func))
            return null;

        functions.add(func);
        return func;
    }

}
