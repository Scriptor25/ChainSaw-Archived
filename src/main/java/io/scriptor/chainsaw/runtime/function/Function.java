package io.scriptor.chainsaw.runtime.function;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.FuncType;
import io.scriptor.chainsaw.runtime.type.Type;

public class Function {

    private transient Environment environment;
    private FuncType type;
    private String id;
    private boolean constructor;
    private Type memberOf;
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

    public boolean isMemberOf(Type memberOf) {
        return this.memberOf == memberOf || (this.memberOf != null && this.memberOf.equals(memberOf));
    }

    public Type getMemberOf() {
        return memberOf;
    }

    public FuncBody getImpl() {
        return impl;
    }

    public boolean equals(FuncType type, String id, Type memberOf) {
        return this.type.equals(type) && this.id.equals(id) && isMemberOf(memberOf);
    }

    public static Function get(Environment env, FuncType type, String id, boolean constructor, Type memberOf) {
        var func = env.getFunction(type, id, memberOf);
        if (func != null)
            return func;

        func = new Function(env, type);
        func.id = id;
        func.constructor = constructor;
        func.memberOf = memberOf;

        return env.addFunction(func);
    }
}
