package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;

public class RetValue extends Value {

    private Value value;

    public RetValue(Environment env, Value value) {
        super(env, value.getType());
        this.value = value;
    }

    public Value getValue() {
        return value;
    }
}
