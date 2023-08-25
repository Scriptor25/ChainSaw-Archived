package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.NativeType;

public class NativeValue<T> extends Value {

    private T value;

    @SuppressWarnings("unchecked")
    public NativeValue(Environment env, T value) {
        this(env, (NativeType<T>) NativeType.get(env, value.getClass()), value);
    }

    public NativeValue(Environment env, NativeType<T> type, T value) {
        super(env, type);
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

}
