package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;

public class VoidType extends Type {

    private VoidType(Environment env) {
        super(env);
    }

    public static VoidType get(Environment env) {
        var type = env.getVoidType();
        if (type != null)
            return type;

        type = new VoidType(env);

        return env.addType(type);
    }

}
