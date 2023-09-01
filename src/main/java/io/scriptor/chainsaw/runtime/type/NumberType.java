package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.NumberValue;
import io.scriptor.chainsaw.runtime.value.Value;

public class NumberType extends Type {

    private NumberType(Environment env) {
        super(env);
    }

    @Override
    public Value nullValue() {
        return new NumberValue(mEnv, 0);
    }

    @Override
    public String toString() {
        return "number";
    }

    public static NumberType get(Environment env) {
        var type = env.getType(NumberType.class);
        if (type != null)
            return type;

        return env.addType(new NumberType(env));
    }
}
