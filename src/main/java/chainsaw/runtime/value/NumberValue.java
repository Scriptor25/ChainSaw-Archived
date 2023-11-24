package chainsaw.runtime.value;

import chainsaw.runtime.Environment;
import chainsaw.runtime.type.NumberType;

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
        if (value instanceof Boolean)
            mValue = ((boolean) value) ? 1 : 0;
        else if (value instanceof Byte)
            mValue = (byte) value;
        else if (value instanceof Short)
            mValue = (short) value;
        else if (value instanceof Integer)
            mValue = (int) value;
        else if (value instanceof Long)
            mValue = (long) value;
        else if (value instanceof Float)
            mValue = (float) value;
        else if (value instanceof Double)
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
