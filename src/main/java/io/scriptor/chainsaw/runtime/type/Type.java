package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.Value;

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

    public abstract Value nullValue();

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

}
