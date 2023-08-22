package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;

public class FloatType extends Type {

    private int bits;

    private FloatType(Environment env) {
        super(env, "FloatType");
    }

    public int getBits() {
        return bits;
    }

    public static FloatType get(Environment env, int bits) {
        var type = env.getFloatType(bits);
        if (type != null)
            return type;

        type = new FloatType(env);
        type.bits = bits;

        return env.addType(type);
    }

    public boolean equals(int bits) {
        return this.bits == bits;
    }
}
