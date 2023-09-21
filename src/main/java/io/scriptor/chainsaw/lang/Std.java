package io.scriptor.chainsaw.lang;

import io.scriptor.chainsaw.runtime.natives.CSawNative;
import io.scriptor.chainsaw.runtime.value.Value;

@CSawNative
public class Std {

    private Std() {
    }

    public static void out(String fmt, Object... args) {
        if (args != null) {
            Object[] newArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++)
                newArgs[i] = (args[i] instanceof Value)
                        ? ((Value) args[i]).getValue()
                        : args[i];

            System.out.printf(fmt, newArgs);
        } else
            System.out.print(fmt);
    }

    public static double inf() {
        return Double.MAX_VALUE;
    }

    public static double random() {
        return Math.random();
    }

    public static double abs(double x) {
        return Math.abs(x);
    }

    public static double pow(double a, double b) {
        return Math.pow(a, b);
    }

    public static double sqrt(double x) {
        return Math.sqrt(x);
    }

    public static double tan(double x) {
        return Math.tan(x);
    }

    public static double floor(double x) {
        return Math.floor(x);
    }
}
