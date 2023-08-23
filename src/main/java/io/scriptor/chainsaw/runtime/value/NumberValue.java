package io.scriptor.chainsaw.runtime.value;

import java.math.BigDecimal;
import java.math.MathContext;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.NumberType;

public class NumberValue extends Value {

    private BigDecimal value;

    public NumberValue(Environment env, NumberType type, boolean value) {
        this(env, type, new BigDecimal(value ? "1" : "0", new MathContext(type.getBits())));
    }

    public NumberValue(Environment env, NumberType type, String value) {
        super(env, type);
        this.value = new BigDecimal(value, new MathContext(type.getBits()));
    }

    public NumberValue(Environment env, NumberType type, BigDecimal value) {
        this(env, type, value.toString());
    }

    public boolean getBool() {
        return value.longValue() != 0;
    }

    @Override
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
