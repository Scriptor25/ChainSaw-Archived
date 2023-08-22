package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.Util;
import io.scriptor.chainsaw.runtime.type.*;

public abstract class Value {

    protected transient Environment environment;
    protected Type type;

    protected Value(Environment env, Type type) {
        this.environment = env;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public static Value get(Environment env, Type type, Object... value) {
        if (type.isVoid())
            return null;

        if (type.isInt())
            return new IntValue(env, (IntType) type, (String) value[0]);

        if (type.isFloat())
            return new FloatValue(env, (FloatType) type, (String) value[0]);

        return Util.error("Value.get not defined for type %s", type);
    }
}
