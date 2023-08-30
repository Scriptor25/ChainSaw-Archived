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

    protected Value(Environment env) {
        this(env, null);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Type getType() {
        return type;
    }

    public abstract Object getValue();

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (!(o instanceof Value))
            return false;
        var v = (Value) o;
        if (!type.isCompat(v.type))
            return false;

        return getValue().equals(v.getValue());
    }

    public static Value extract(Value value) {
        if (value == null)
            return null;

        if (!(value.getValue() instanceof Value))
            return value;

        return extract((Value) value.getValue());
    }

    @SuppressWarnings("unchecked")
    public static Value parseValue(Environment env, Object value, Type type) {
        if (type == null)
            return null;

        if (type.isVoid())
            return new Value(env, type) {
                @Override
                public Object getValue() {
                    return value;
                }
            };

        if (type.isNumber())
            return new NumberValue(env, (double) value);

        if (type.isString())
            return new StringValue(env, (CharSequence) value);

        if (type.isNative())
            return new NativeValue<>(env, (NativeType<Object>) type, value);

        return Util.error("error");
    }

}
