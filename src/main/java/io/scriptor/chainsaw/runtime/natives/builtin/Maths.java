package io.scriptor.chainsaw.runtime.natives.builtin;

import io.scriptor.chainsaw.runtime.natives.CSawFunction;
import io.scriptor.chainsaw.runtime.natives.CSawParam;

public class Maths {

    @CSawFunction
    public static double sqrt(@CSawParam double x) {
        return Math.sqrt(x);
    }

    @CSawFunction
    public static double floor(@CSawParam double x) {
        return Math.floor(x);
    }

    @CSawFunction
    public static double inf() {
        return Double.MAX_VALUE;
    }

    @CSawFunction
    public static double random() {
        return Math.random();
    }
}
