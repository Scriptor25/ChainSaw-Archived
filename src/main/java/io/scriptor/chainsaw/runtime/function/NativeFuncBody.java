package io.scriptor.chainsaw.runtime.function;

import java.util.Map;

import io.scriptor.chainsaw.runtime.value.Value;

public class NativeFuncBody extends FuncBody {

    @FunctionalInterface
    public static interface NativeFunc {
        public Value run(Map<String, Value> args) throws Exception;
    }

    private transient NativeFunc func;

    public NativeFuncBody(Function function, NativeFunc runnable) {
        super(function);
        this.func = runnable;
    }

    public Value run(Map<String, Value> args) {
        try {
            return func.run(args);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
