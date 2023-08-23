package io.scriptor.chainsaw.runtime;

import java.util.List;

import io.scriptor.chainsaw.runtime.value.Value;

public class NativeFuncBody extends FuncBody {

    @FunctionalInterface
    public static interface NativeFunc {
        public Value run(List<Value> args);
    }

    private NativeFunc func;

    public NativeFuncBody(Function function, NativeFunc runnable) {
        super(function);
        this.func = runnable;
    }

    public Value run(List<Value> args) {
        return func.run(args);
    }
}
