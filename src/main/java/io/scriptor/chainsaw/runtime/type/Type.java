package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.Error;
import io.scriptor.chainsaw.runtime.value.Value;

public abstract class Type {

    protected final Environment mEnv;

    protected Type(Environment env) {
        mEnv = env;
    }

    public abstract Value nullValue();

    public boolean isVoid() {
        return this instanceof VoidType;
    }

    public boolean isNumber() {
        return this instanceof NumberType;
    }

    public boolean isChar() {
        return this instanceof CharType;
    }

    public boolean isString() {
        return this instanceof StringType;
    }

    public boolean isThing() {
        return false;
    }

    public boolean isNative() {
        return false;
    }

    public static Type parseType(Environment env, String str) {
        if (str == null)
            return null;

        switch (str) {
            case "void":
                return VoidType.get(env);
            case "number":
                return NumberType.get(env);
            case "char":
                return CharType.get(env);
            case "string":
                return StringType.get(env);
        }

        Type type = ThingType.get(env, str);
        if (type != null)
            return type;

        type = NativeType.get(env, null, str);
        if (type != null)
            return type;

        return Error.error("undefined type '%s'", str);
    }

}
