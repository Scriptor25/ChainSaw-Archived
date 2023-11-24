package chainsaw.runtime.type;

import chainsaw.runtime.Environment;
import chainsaw.runtime.value.Value;
import chainsaw.runtime.value.VoidValue;

public class VoidType extends Type {

    private VoidType(Environment env) {
        super(env);
    }

    @Override
    public Value nullValue() {
        return new VoidValue(mEnv, null);
    }

    @Override
    public String toString() {
        return "void";
    }

    public static VoidType get(Environment env) {
        var type = env.getType(VoidType.class);
        if (type != null)
            return type;

        return env.addType(new VoidType(env));
    }
}
