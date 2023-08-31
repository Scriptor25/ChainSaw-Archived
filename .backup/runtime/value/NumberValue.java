package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.NumberType;

public class NumberValue extends Value {

    private double value;

    public NumberValue(Environment env, boolean value) {
        this(env, value ? 1 : 0);
    }

    public NumberValue(Environment env, String value) {
        this(env, Double.parseDouble(value));
    }

    public NumberValue(Environment env, double value) {
        super(env, NumberType.get(env));
        this.value = value;
    }

    public boolean getBool() {
        return value != 0;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return (value == Math.floor(value)) ? Long.toString((long) value) : Double.toString(value);
    }

}
