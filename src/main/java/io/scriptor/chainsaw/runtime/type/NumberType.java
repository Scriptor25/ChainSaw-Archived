package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;

public class NumberType extends Type {

    private int bits;

    private NumberType(Environment env) {
        super(env);
    }

    public int getBits() {
        return bits;
    }

    public static NumberType get(Environment env, int bits) {
        var type = env.getNumberType(bits);
        if (type != null)
            return type;

        type = new NumberType(env);
        type.bits = bits;

        return env.addType(type);
    }

    public boolean equals(int bits) {
        return this.bits == bits;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && equals(((NumberType) o).bits);
    }

}
