package io.scriptor.chainsaw.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.scriptor.chainsaw.Util;
import io.scriptor.chainsaw.runtime.function.Function;
import io.scriptor.chainsaw.runtime.function.Pair;
import io.scriptor.chainsaw.runtime.natives.NativesCollector;
import io.scriptor.chainsaw.runtime.type.NativeType;
import io.scriptor.chainsaw.runtime.type.ThingType;
import io.scriptor.chainsaw.runtime.type.Type;
import io.scriptor.chainsaw.runtime.value.Value;

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
        NativesCollector.collect(this);
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
                return Util.error("undefined variable '%s'", name);

        return (V) mVariables.get(name);
    }

    public <V extends Value> V createVariable(String name, V value) {
        if (mVariables.containsKey(name))
            return Util.error("variable '%s' already defined", name);

        mVariables.put(name, value);
        return value;
    }

    public <V extends Value> V setVariable(String name, V value) {
        if (!mVariables.containsKey(name))
            if (mParent != null)
                return mParent.setVariable(name, value);
            else
                return Util.error("undefined variable '%s'", name);

        if (!mVariables.get(name).getType().equals(value.getType()))
            return Util.error("cannot assign value of type %s to variable '%s' (type %s)",
                    value.getType(),
                    name,
                    mVariables.get(name).getType());

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
