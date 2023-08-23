package io.scriptor.chainsaw.runtime.type;

import java.util.Collections;
import java.util.List;

import io.scriptor.chainsaw.runtime.Environment;

public class FuncType extends Type {

    private Type result;
    private List<FuncParam> params;
    private boolean vararg;

    private FuncType(Environment env) {
        super(env);
    }

    public Type getResult() {
        return result;
    }

    public List<FuncParam> getParams() {
        return Collections.unmodifiableList(params);
    }

    public boolean isVararg() {
        return vararg;
    }

    public static FuncType get(Environment env, Type result, List<FuncParam> params, boolean vararg) {
        var type = env.getFuncType(result, params, vararg);
        if (type != null)
            return type;

        type = new FuncType(env);
        type.result = result;
        type.params = params;
        type.vararg = vararg;

        return env.addType(type);
    }

    public boolean equals(Type result, List<FuncParam> params, boolean vararg) {
        return this.result.equals(result) && (this.params.equals(params) || (this.params.isEmpty() && params == null))
                && this.vararg == vararg;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && equals(((FuncType) o).result, ((FuncType) o).params, ((FuncType) o).vararg);
    }
}
