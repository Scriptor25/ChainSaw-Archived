package io.scriptor;

import io.scriptor.chainsaw.runtime.natives.CSawFunction;

public class Breakpoint {

    @CSawFunction
    public static void bp() {
        System.out.println("breakpoint reached");
    }
}
