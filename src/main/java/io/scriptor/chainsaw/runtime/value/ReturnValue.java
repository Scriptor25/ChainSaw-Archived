package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;

public class ReturnValue extends Value {

    private Value mValue;
    private int mDepth;

    public ReturnValue(Value value, int depth) {
        super(value.getType());
        mValue = value;
        mDepth = depth;
    }

    @Override
    public Value getValue() {
        return mValue;
    }

    @Override
    public ReturnValue setValue(Object value) {
        mValue = (Value) value;
        return this;
    }

    public int getDepth() {
        return mDepth;
    }

    public boolean isReceiver(Environment env) {
        return env.getDepth() < mDepth;
    }

    public boolean isFrom(Environment env) {
        return env.getDepth() == mDepth;
    }

}
