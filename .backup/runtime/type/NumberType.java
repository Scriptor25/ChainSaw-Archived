package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.NumberValue;
import io.scriptor.chainsaw.runtime.value.Value;

public class NumberType extends Type {

    private NumberType(Environment env) {
        super(env);
    }

    @Override
    public Value emptyValue() {
        return new NumberValue(environment, 0);
    }

    public static NumberType get(Environment env) {
        var type = env.getNumberType();
        if (type != null)
            return type;

        type = new NumberType(env);

        return env.addType(type);
    }

}
