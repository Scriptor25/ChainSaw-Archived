package io.scriptor.chainsaw.runtime.natives;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CSawFunction {

    /**
     * the function id
     */
    String id();

    /**
     * the function return type; string representation
     */
    String result() default "void";

    /**
     * the function parameters; string representation: "<name>: <type>"
     */
    String[] params() default {};

    /**
     * if the function is vararg
     */
    boolean vararg() default false;

    /**
     * if the function is a constructor
     */
    boolean constructor() default false;

    /**
     * the parent type of the function
     */
    String memberOf() default "";
}
