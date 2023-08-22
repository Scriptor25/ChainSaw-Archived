package io.scriptor.chainsaw.runtime.type;

import java.util.Collections;
import java.util.Map;

import io.scriptor.chainsaw.runtime.Environment;

public class FuncType extends Type {

    private Type result;
    private Map<String, Type> params;
    private boolean vararg;

    private FuncType(Environment env) {
        super(env, "FuncType");
    }

    public Type getResult() {
        return result;
    }

    public Map<String, Type> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public boolean isVararg() {
        return vararg;
    }

    public static FuncType get(Environment env, Type result, Map<String, Type> params, boolean vararg) {
        var type = env.getFuncType(result, params, vararg);
        if (type != null)
            return type;

        type = new FuncType(env);
        type.result = result;
        type.params = params;
        type.vararg = vararg;

        return env.addType(type);
    }

    public boolean equals(Type result, Map<String, Type> params, boolean vararg) {
        return this.result.equals(result) && (this.params.equals(params) || (this.params.isEmpty() && params == null))
                && this.vararg == vararg;
    }
}
