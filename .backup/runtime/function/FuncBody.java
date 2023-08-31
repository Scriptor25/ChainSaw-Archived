package io.scriptor.chainsaw.runtime.function;

public abstract class FuncBody {

    protected transient Function function;

    protected FuncBody(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

}
