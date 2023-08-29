package io.scriptor;

import java.util.Map;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.natives.CSawFunction;
import io.scriptor.chainsaw.runtime.value.Value;

public class Breakpoint {

    @CSawFunction(id = "bp")
    public static Value bp(Environment env, Map<String, Value> params) {
        System.out.println("breakpoint reached");
        return null;
    }
}
