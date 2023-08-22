package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.FuncType;

public class Function extends Value {

    private String id;
    private FuncBody impl;

    private Function(Environment env, FuncType type) {
        super(env, type);
    }

    public void setImpl(FuncBody impl) {
        if (this.impl != null || impl == null)
            return;

        this.impl = impl;
    }

    public String getId() {
        return id;
    }

    public FuncBody getImpl() {
        return impl;
    }

    public static Function get(Environment env, FuncType type, String id) {
        var func = env.getFunction(type, id);
        if (func != null)
            return func;

        func = new Function(env, type);
        func.id = id;

        return env.addFunction(func);
    }

    public boolean equals(FuncType type, String id) {
        return this.getType().equals(type) && this.id.equals(id);
    }

}
