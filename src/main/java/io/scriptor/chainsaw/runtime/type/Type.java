package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.Util;

import static io.scriptor.chainsaw.Constants.*;

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

    public boolean isCompat(Type type) {
        if (type == null)
            return false;
        if (type == this)
            return true;

        return getClass().isInstance(type);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;

        return getClass().isInstance(o);
    }

    public static Type getBest(Type a, Type b) {
        if (a.equals(b))
            return a;

        if (a.isNumber() && b.isNumber())
            return ((NumberType) a).getBits() > ((NumberType) b).getBits() ? a : b;

        return Util.error("");
    }

}
