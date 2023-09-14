package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.VoidType;

public class VoidValue extends Value {

    private Object mValue;

    public VoidValue(Environment env, Object value) {
        super(VoidType.get(env));
        mValue = value;
    }

    @Override
    public Object getValue() {
        return mValue;
    }

    @Override
    public VoidValue setValue(Object value) {
        mValue = value;
        return this;
    }

}
