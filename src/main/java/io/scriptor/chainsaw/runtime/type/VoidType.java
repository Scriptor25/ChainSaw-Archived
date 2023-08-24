package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.Value;

public class VoidType extends Type {

    private VoidType(Environment env) {
        super(env);
    }

    @Override
    public Value nullValue() {
        return null;
    }

    public static VoidType get(Environment env) {
        var type = env.getVoidType();
        if (type != null)
            return type;

        type = new VoidType(env);

        return env.addType(type);
    }

}
