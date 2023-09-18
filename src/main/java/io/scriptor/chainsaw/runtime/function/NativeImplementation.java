package io.scriptor.chainsaw.runtime.function;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.Value;

public class NativeImplementation implements FunctionImplementation {

    private final Method mMethod;
    private final Constructor<?> mConstructor;

    public NativeImplementation(Method mthd) {
        mMethod = mthd;
        mConstructor = null;
    }

    public NativeImplementation(Constructor<?> cons) {
        mMethod = null;
        mConstructor = cons;
    }

    public Value invoke(Environment env, Object my, Object... args) {
        try {
            if (mMethod != null)
                return Value.fromObject(env, mMethod.invoke(my, args));

            if (mConstructor != null)
                return Value.fromObject(env, mConstructor.newInstance(args));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
