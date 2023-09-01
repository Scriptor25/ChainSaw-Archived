package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.NativeValue;
import io.scriptor.chainsaw.runtime.value.Value;

public class NativeType<C> extends Type {

    private final Class<C> mNativeClass;
    private final String mAlias;

    private NativeType(Environment env, Class<C> nativeClass, String alias) {
        super(env);
        mNativeClass = nativeClass;
        mAlias = alias;
    }

    public Class<C> getNativeClass() {
        return mNativeClass;
    }

    public String getAlias() {
        return mAlias;
    }

    @Override
    public Value nullValue() {
        return new NativeValue<>(mEnv, this, null);
    }

    @Override
    public String toString() {
        return mAlias;
    }

    public static <C> NativeType<C> get(Environment env, Class<C> nativeClass, String alias) {
        var type = env.getNativeType(nativeClass, alias);
        if (type != null)
            return type;

        type = new NativeType<>(env, nativeClass, alias);

        return env.addNativeType(type);
    }

}
