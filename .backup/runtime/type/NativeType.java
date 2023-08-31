package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.NativeValue;
import io.scriptor.chainsaw.runtime.value.Value;

public class NativeType<T> extends Type {

    private transient Class<T> nativeClass;
    private String alias;

    private NativeType(Environment env) {
        super(env);
    }

    public Class<T> getNativeClass() {
        return nativeClass;
    }

    public boolean equals(Class<?> nativeClass, String alias) {
        if (nativeClass == null && alias == null)
            return false;
        return (nativeClass == null || this.nativeClass.equals(nativeClass))
                && (alias == null || this.alias.equals(alias));
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && equals(((NativeType<?>) o).nativeClass, ((NativeType<?>) o).alias);
    }

    @Override
    public Value emptyValue() {
        return new NativeValue<T>(environment, this, null);
    }

    public static <T> NativeType<T> get(Environment env, String alias) {
        return env.getNativeType(null, alias);
    }

    public static <T> NativeType<T> get(Environment env, Class<T> nativeClass) {
        return env.getNativeType(nativeClass, null);
    }

    public static <T> NativeType<T> create(Environment env, Class<T> nativeClass, String alias) {
        NativeType<T> type = env.getNativeType(nativeClass, alias);
        if (type != null)
            return type;

        type = new NativeType<>(env);
        type.nativeClass = nativeClass;
        type.alias = alias;

        return env.addType(type);
    }

}
