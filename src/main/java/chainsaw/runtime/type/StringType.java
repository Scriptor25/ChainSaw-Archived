package chainsaw.runtime.type;

import chainsaw.runtime.Environment;
import chainsaw.runtime.value.StringValue;
import chainsaw.runtime.value.Value;

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
