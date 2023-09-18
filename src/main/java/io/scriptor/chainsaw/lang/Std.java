package io.scriptor.chainsaw.lang;

import io.scriptor.chainsaw.runtime.natives.CSawNative;
import io.scriptor.chainsaw.runtime.value.NumberValue;
import io.scriptor.chainsaw.runtime.value.StringValue;

@CSawNative()
public class Std {

    private Std() {
    }

    public static void out(StringValue fmt, Object... args) {
        System.out.printf(fmt.getValue(), args);
    }

    public static double inf() {
        return Double.MAX_VALUE;
    }

    public static double random() {
        return Math.random();
    }

    public static double abs(NumberValue x) {
        return Math.abs(x.getValue());
    }

    public static double pow(NumberValue a, NumberValue b) {
        return Math.pow(a.getValue(), b.getValue());
    }

    public static double sqrt(NumberValue x) {
        return Math.sqrt(x.getValue());
    }

    public static double tan(NumberValue x) {
        return Math.tan(x.getValue());
    }

    public static double floor(NumberValue x) {
        return Math.floor(x.getValue());
    }
}
