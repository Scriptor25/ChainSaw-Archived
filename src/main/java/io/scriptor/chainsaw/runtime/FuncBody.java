package io.scriptor.chainsaw.runtime;

public abstract class FuncBody {

    protected transient Function function;

    protected FuncBody(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }
}
