package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.Util;

import static io.scriptor.chainsaw.Constants.*;

public abstract class Type {

    protected transient Environment environment;
    private final String name;

    protected Type(Environment env, String name) {
        this.environment = env;
        this.name = name;
    }

    public boolean isInt() {
        return this instanceof IntType;
    }

    public boolean isFloat() {
        return this instanceof FloatType;
    }

    public boolean isVoid() {
        return this instanceof VoidType;
    }

    public boolean isThing() {
        return this instanceof ThingType;
    }

    public static Type getBest(Type a, Type b) {
        if (a.isInt() && b.isInt()) {
            var ia = (IntType) a;
            var ib = (IntType) b;

            return ia.getBits() > ib.getBits() ? ia : ib;
        }

        if (a.isFloat() && b.isFloat()) {
            var fa = (FloatType) a;
            var fb = (FloatType) b;

            return fa.getBits() > fb.getBits() ? fa : fb;
        }

        if (a.isFloat() && b.isInt())
            return a;

        if (a.isInt() && b.isFloat())
            return b;

        return Util.error("types '%s' and '%s' are not compatible with float or int", a, b);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
