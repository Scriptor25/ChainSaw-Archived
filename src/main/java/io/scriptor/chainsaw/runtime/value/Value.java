package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
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

}
