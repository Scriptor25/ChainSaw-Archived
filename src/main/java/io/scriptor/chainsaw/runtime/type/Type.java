package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.Util;
import io.scriptor.chainsaw.runtime.value.Value;

public abstract class Type {

    protected transient Environment environment;

    protected Type(Environment env) {
        this.environment = env;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public boolean isVoid() {
        return this instanceof VoidType;
    }

    public boolean isNumber() {
        return this instanceof NumberType;
    }

    public boolean isThing() {
        return this instanceof ThingType;
    }

    public boolean isFunc() {
        return this instanceof FuncType;
    }

    public boolean isString() {
        return this instanceof StringType;
    }

    public boolean isNative() {
        return this instanceof NativeType;
    }

    public boolean isCompat(Type type) {
        if (type == null)
            return false;
        if (type == this)
            return true;

        return getClass().isInstance(type);
    }

    public abstract Value emptyValue();

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;

        return getClass().isInstance(o);
    }

    public static Type parseType(Environment env, String type) {
        if (type == null || type.isEmpty())
            return null;

        switch (type) {
            case "num":
                return NumberType.get(env);
            case "str":
                return StringType.get(env);
            case "void":
                return VoidType.get(env);
        }

        Type t = ThingType.get(env, type);
        if (t != null)
            return t;

        t = NativeType.get(env, type);
        if (t != null)
            return t;

        return Util.error("undefined Type '%s'", type);
    }

}
