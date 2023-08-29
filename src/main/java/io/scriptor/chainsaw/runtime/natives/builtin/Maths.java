package io.scriptor.chainsaw.runtime.natives.builtin;

import java.util.Map;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.natives.CSawFunction;
import io.scriptor.chainsaw.runtime.value.NumberValue;
import io.scriptor.chainsaw.runtime.value.Value;

public class Maths {

    @CSawFunction(id = "sqrt", result = "num", params = { "x: num" })
    public static Value sqrt(Environment env, Map<String, Value> params) {
        return new NumberValue(env, Math.sqrt((double) params.get("x").getValue()));
    }

    @CSawFunction(id = "floor", result = "num", params = { "x: num" })
    public static Value floor(Environment env, Map<String, Value> params) {
        return new NumberValue(env, Math.floor((double) params.get("x").getValue()));
    }

    @CSawFunction(id = "inf", result = "num")
    public static Value inf(Environment env, Map<String, Value> params) {
        return new NumberValue(env, Double.MAX_VALUE);
    }

    @CSawFunction(id = "random", result = "num")
    public static Value random(Environment env, Map<String, Value> params) {
        return new NumberValue(env, Math.random());
    }
}
