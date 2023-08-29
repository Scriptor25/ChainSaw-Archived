package io.scriptor.chainsaw.runtime.natives.builtin;

import java.util.Map;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.natives.CSawFunction;
import io.scriptor.chainsaw.runtime.value.Value;

public class IO {

    @CSawFunction(id = "out", params = { "fmt: str" }, vararg = true)
    public static Value out(Environment env, Map<String, Value> params) {
        String fmt = (String) params.get("fmt").getValue();
        Object[] args = new Object[params.size() - 1];
        for (int i = 0; i < args.length; i++)
            args[i] = params.get("vararg" + i);

        System.out.printf(fmt, args);

        return null;
    }

}
