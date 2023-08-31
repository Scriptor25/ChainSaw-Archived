package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.Value;

public class VoidType extends Type {

    private VoidType(Environment env) {
        super(env);
    }

    @Override
    public Value emptyValue() {
        return null;
    }

    @Override
    public boolean isCompat(Type type) {
        return true;
    }

    public static VoidType get(Environment env) {
        var type = env.getVoidType();
        if (type != null)
            return type;

        type = new VoidType(env);

        return env.addType(type);
    }

}
