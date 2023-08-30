package io.scriptor.chainsaw.runtime.natives.builtin;

import io.scriptor.chainsaw.runtime.natives.CSawFunction;
import io.scriptor.chainsaw.runtime.natives.CSawParam;

public class IO {

    @CSawFunction
    public static void out(@CSawParam String fmt, @CSawParam Object... args) {
        System.out.printf(fmt, args);
    }

}
