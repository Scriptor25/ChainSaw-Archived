package chainsaw.runtime.type;

import chainsaw.runtime.Environment;
import chainsaw.runtime.value.NumberValue;
import chainsaw.runtime.value.Value;

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
