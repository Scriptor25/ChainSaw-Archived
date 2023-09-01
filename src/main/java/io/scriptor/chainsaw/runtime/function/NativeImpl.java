package io.scriptor.chainsaw.runtime.function;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.Error;
import io.scriptor.chainsaw.runtime.value.Value;

public class NativeImpl implements FunctionImpl {

    @FunctionalInterface
    public static interface NativeFunction {

        Value invoke(Environment env) throws Exception;
    }

    private final NativeFunction mFunction;

    public NativeImpl(NativeFunction function) {
        mFunction = function;
    }

    public Value invoke(Environment env) {
        try {
            return mFunction.invoke(env);
        } catch (Exception e) {
            Error.error("from native function: %s", e.getMessage());
            return null;
        }
    }
}
