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

    public Value run(Environment env, Object member, Map<String, Object> args) {
        try {
            int pcount = func.getParameterCount();
            var fparams = func.getParameters();
            Object[] params = new Object[args.size()];
            int i = 0;
            for (; i < pcount; i++) {
                if (fparams[i].isVarArgs()) {
                    int startI = i;
                    for (; i < params.length; i++)
                        params[i] = args.get("vararg" + (i - startI));

                    break;
                }
                params[i] = args.get(fparams[i].getName());
            }

            return Value.parseValue(env, func.invoke(member, params), function.getType().getResult());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
