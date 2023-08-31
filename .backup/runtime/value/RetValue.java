package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;

public class RetValue extends Value {

    private Value value;
    private int depth;

    public RetValue(Environment env, Value value) {
        super(env, value.getType());
        this.value = value;
        this.depth = env.getDepth();
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public static boolean forMe(Environment env, Value value) {
        if (!(value instanceof RetValue))
            return false;

        var retval = (RetValue) value;
        return retval.depth >= env.getDepth();
    }

}
