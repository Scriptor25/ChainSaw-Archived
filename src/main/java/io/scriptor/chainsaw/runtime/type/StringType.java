package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.StringValue;
import io.scriptor.chainsaw.runtime.value.Value;

public class StringType extends Type {

    private StringType(Environment env) {
        super(env);
    }

    @Override
    public Value nullValue() {
        return new StringValue(mEnv, "");
    }

    @Override
    public String toString() {
        return "string";
    }

    public static StringType get(Environment env) {
        var type = env.getType(StringType.class);
        if (type != null)
            return type;

        return env.addType(new StringType(env));
    }
}
