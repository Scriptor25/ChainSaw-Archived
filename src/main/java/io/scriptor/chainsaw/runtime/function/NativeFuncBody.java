package io.scriptor.chainsaw.runtime.function;

import java.lang.reflect.Method;
import java.util.Map;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.Value;

public class NativeFuncBody extends FuncBody {

    private transient Method func;

    public NativeFuncBody(Function function, Method runnable) {
        super(function);
        this.func = runnable;
    }

    public Value run(Environment env, Map<String, Value> args) {
        try {
            return (Value) func.invoke(null, env, args);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
