package io.scriptor.chainsaw.lang;

import io.scriptor.chainsaw.runtime.natives.CSawNative;

@CSawNative
public class Std {

    private Std() {
    }

    public static void out(String fmt, Object... args) {
        System.out.printf(fmt, args);
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
