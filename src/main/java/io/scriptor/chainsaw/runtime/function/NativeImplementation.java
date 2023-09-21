package io.scriptor.chainsaw.runtime.function;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.scriptor.chainsaw.Error;
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
            args = castArgs(mMethod != null ? mMethod : mConstructor != null ? mConstructor : null, args);

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

    private static Object[] castArgs(Executable exec, Object[] args) {

        if (exec == null || args == null)
            return args;

        final var params = exec.getParameterTypes();
        final var count = exec.getParameterCount();
        final var newArgs = new Object[args.length];

        for (int i = 0; i < count; i++)
            newArgs[i] = castArg(args[i], params[i]);

        return newArgs;
    }

    private static Object castArg(Object object, Class<?> type) {
        if (object == null || type == null)
            return null;

        if (object.getClass().isArray()) {
            System.out.println("hi");

            final var src = (Object[]) object;
            final var dst = new Object[src.length];
            for (int i = 0; i < src.length; i++)
                dst[i] = castArg(src[i], type.getComponentType());
            return dst;
        }

        if (type.equals(object.getClass()) || type.equals(Object.class))
            return object;

        if (object instanceof Value) {
            final var value = ((Value) object).getValue();
            if (value.getClass().isPrimitive())
                return type.cast(value);

            if (value instanceof Double)
                return type.cast(((Double) value).doubleValue());
        }

        return Error.error("you are a complete faaaailiure!");
    }
}
