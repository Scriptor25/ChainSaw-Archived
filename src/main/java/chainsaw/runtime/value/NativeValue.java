package chainsaw.runtime.value;

import chainsaw.runtime.Environment;
import chainsaw.runtime.type.NativeType;

public class NativeValue<C> extends Value {

    private C mValue;

    public NativeValue(Environment env, NativeType<C> type, C value) {
        super(type);
        mValue = value;
    }

    @Override
    public C getValue() {
        return mValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NativeValue<C> setValue(Object value) {
        mValue = (C) value;
        return this;
    }

}
