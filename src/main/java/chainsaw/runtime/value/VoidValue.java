package chainsaw.runtime.value;

import chainsaw.runtime.Environment;
import chainsaw.runtime.type.VoidType;

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
