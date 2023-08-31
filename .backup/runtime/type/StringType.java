package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.StringValue;
import io.scriptor.chainsaw.runtime.value.Value;

public class StringType extends Type {

    private StringType(Environment env) {
        super(env);
    }

    @Override
    public Value emptyValue() {
        return new StringValue(environment, "");
    }

    public static StringType get(Environment env) {
        var type = env.getStringType();
        if (type != null)
            return type;

        type = new StringType(env);

        return env.addType(type);
    }

}
