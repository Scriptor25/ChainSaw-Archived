package io.scriptor.chainsaw.runtime.value;

import java.math.BigDecimal;
import java.math.MathContext;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.FloatType;

public class FloatValue extends Value {

    private BigDecimal value;

    public FloatValue(Environment env, FloatType type, String value) {
        super(env, type);
        this.value = new BigDecimal(value, new MathContext(type.getBits()));
    }

    public BigDecimal getValue() {
        return value;
    }

}
