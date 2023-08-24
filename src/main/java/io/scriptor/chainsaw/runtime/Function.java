package io.scriptor.chainsaw.runtime;

import io.scriptor.chainsaw.runtime.type.FuncType;

public class Function {

    private transient Environment environment;
    private FuncType type;
    private String id;
    private boolean constructor;
    private FuncBody impl;

    private Function(Environment env, FuncType type) {
        this.environment = env;
        this.type = type;
    }

    public void setImpl(FuncBody impl) {
        if (this.impl != null || impl == null)
            return;

        this.impl = impl;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public FuncType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public boolean isConstructor() {
        return constructor;
    }

    public FuncBody getImpl() {
        return impl;
    }

    public boolean equals(FuncType type, String id) {
        return this.type.equals(type) && this.id.equals(id);
    }

    public static Function get(Environment env, FuncType type, String id, boolean constructor) {
        var func = env.getFunction(type, id);
        if (func != null)
            return func;

        func = new Function(env, type);
        func.id = id;
        func.constructor = constructor;

        return env.addFunction(func);
    }
}
