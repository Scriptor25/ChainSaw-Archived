package chainsaw.runtime.type;

import chainsaw.runtime.Environment;
import chainsaw.runtime.value.NativeValue;
import chainsaw.runtime.value.Value;

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

    public static <C> NativeType<C> create(Environment env, Class<C> nativeClass, String alias) {
        var type = env.getNativeType(nativeClass, alias);
        if (type != null)
            return type;

        type = new NativeType<>(env, nativeClass, alias);

        return env.addNativeType(type);
    }

    public static <C> NativeType<C> get(Environment env, Class<C> nativeClass) {
        return env.getNativeType(nativeClass, null);
    }

    public static <C> NativeType<C> get(Environment env, String alias) {
        return env.getNativeType(null, alias);
    }

}
