package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.NumberType;

public class NumberValue extends Value {

    private double mValue;

    public NumberValue(Environment env, double value) {
        super(NumberType.get(env));
        mValue = value;
    }

    @Override
    public Double getValue() {
        return mValue;
    }

    @Override
    public NumberValue setValue(Object value) {
        mValue = (double) value;
        return this;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && ((NumberValue) other).mValue == mValue;
    }

    @Override
    public String toString() {
        return (mValue == Math.floor(mValue)) ? Long.toString((long) mValue) : Double.toString(mValue);
    }

}
