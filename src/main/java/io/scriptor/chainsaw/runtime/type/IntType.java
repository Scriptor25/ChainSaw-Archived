package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;

public class IntType extends Type {

    private int bits;

    private IntType(Environment env) {
        super(env, "IntType");
    }

    public int getBits() {
        return bits;
    }

    public static IntType get(Environment env, int bits) {
        var type = env.getIntType(bits);
        if (type != null)
            return type;

        type = new IntType(env);
        type.bits = bits;

        return env.addType(type);
    }

    public boolean equals(int bits) {
        return this.bits == bits;
    }
}
