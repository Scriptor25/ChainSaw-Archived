package chainsaw.runtime.function;

import java.util.Collections;
import java.util.List;

import chainsaw.runtime.Environment;
import chainsaw.runtime.type.Type;

public class Function {

    private final boolean mIsConstructor;
    private final String mName;
    private final Type mResultType;
    private final List<Pair<String, Type>> mParameters;
    private final boolean mIsVarArg;
    private final Type mSuperType;
    private FunctionImplementation mImplementation;

    public Function(
            boolean isConstructor,
            String name,
            Type resultType,
            List<Pair<String, Type>> parameters,
            boolean isVararg,
            Type superType,
            FunctionImplementation implementation) {
        mIsConstructor = isConstructor;
        mName = name;
        mResultType = resultType;
        mParameters = parameters;
        mIsVarArg = isVararg;
        mSuperType = superType;
        mImplementation = implementation;
    }

    public boolean isConstructor() {
        return mIsConstructor;
    }

    public String getName() {
        return mName;
    }

    public Type getResultType() {
        return mResultType;
    }

    public List<Pair<String, Type>> getParameters() {
        return Collections.unmodifiableList(mParameters);
    }

    public boolean isVarArg() {
        return mIsVarArg;
    }

    public Type getSuperType() {
        return mSuperType;
    }

    public FunctionImplementation getImplementation() {
        return mImplementation;
    }

    public boolean isOpaque() {
        return mImplementation == null;
    }

    public boolean setImplementation(FunctionImplementation impl) {
        if (isOpaque())
            return false;

        mImplementation = impl;
        return true;
    }

    public boolean isSimilar(String name, List<Pair<String, Type>> parameters, boolean isVararg, Type superType) {
        return mName.equals(name) &&
                mParameters.equals(parameters) &&
                (mIsVarArg == isVararg) &&
                (mSuperType == superType || mSuperType.equals(superType));
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof Function))
            return false;

        var func = (Function) other;

        return (mIsConstructor == func.mIsConstructor) &&
                mName.equals(func.mName) &&
                (mResultType == func.mResultType || (mResultType != null && mResultType.equals(func.mResultType))) &&
                mParameters.equals(func.mParameters) &&
                (mIsVarArg == func.mIsVarArg) &&
                mSuperType.equals(func.mSuperType) &&
                mImplementation.equals(func.mImplementation);
    }

    public static Function get(
            Environment env,
            boolean isConstructor,
            String name,
            Type resultType,
            List<Pair<String, Type>> parameters,
            boolean isVararg,
            Type superType,
            FunctionImplementation implementation) {

        var func = env.getFunction(name, parameters, isVararg, superType);
        if (func != null) {
            if (implementation == null || func.setImplementation(implementation))
                return func;

            return null;
        }

        func = new Function(isConstructor, name, resultType, parameters, isVararg, superType, implementation);

        return env.addFunction(func);
    }

}
