package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.Value;
import io.scriptor.chainsaw.runtime.value.VoidValue;

public class VoidType extends Type {

    private VoidType(Environment env) {
        super(env);
    }

    @Override
    public Value nullValue() {
        return new VoidValue(mEnv, null);
    }

    @Override
    public String toString() {
        return "void";
    }

    public static VoidType get(Environment env) {
        var type = env.getType(VoidType.class);
        if (type != null)
            return type;

        return env.addType(new VoidType(env));
    }
}
