package io.scriptor.chainsaw.runtime.value;

public abstract class FuncBody {

    protected Function function;

    protected FuncBody(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }
}
