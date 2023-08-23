package io.scriptor.chainsaw.runtime.value;

import java.math.BigDecimal;
import java.util.Map;

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

    public Value extract() {
        if (!(getValue() instanceof Value))
            return this;

        return ((Value) getValue()).extract();
    }

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

    public static Value get(Environment env, Type type, Object... value) {
        if (type.isVoid())
            return null;

        if (type.isNumber()) {
            if (value.length == 0)
                return new NumberValue(env, (NumberType) type, "0");

            var v = value[0];
            if (v instanceof String)
                return new NumberValue(env, (NumberType) type, (String) v);
            if (v instanceof BigDecimal)
                return new NumberValue(env, (NumberType) type, (BigDecimal) v);
        }

        if (type.isThing()) {
            if (value.length == 0)
                return new ThingValue(env, (ThingType) type);

            var v = value[0];
            if (v instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Value> fields = (Map<String, Value>) v;
                return new ThingValue(env, (ThingType) type, fields);
            }
        }

        return Util.error("cannot get value for type %s using value(s) %s", type, value);
    }

    public static Value cast(Value value, Type dest) {
        var type = value.getType();

        if (type.equals(dest))
            return value;

        if (type.isNumber() && dest.isNumber())
            return new NumberValue(value.getEnvironment(), (NumberType) dest, (BigDecimal) value.getValue());

        return Util.error("undefined cast from %s to %s", type, dest);
    }
}
