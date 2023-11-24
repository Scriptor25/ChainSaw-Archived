package chainsaw.runtime.type;

import chainsaw.runtime.Environment;
import chainsaw.runtime.value.CharValue;
import chainsaw.runtime.value.Value;

public class CharType extends Type {

    private CharType(Environment env) {
        super(env);
    }

    @Override
    public Value nullValue() {
        return new CharValue(mEnv, (char) 0);
    }

    @Override
    public String toString() {
        return "char";
    }

    public static CharType get(Environment env) {
        var type = env.getType(CharType.class);
        if (type != null)
            return type;

        return env.addType(new CharType(env));
    }
}
