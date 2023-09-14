package io.scriptor.chainsaw.runtime.function;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.Type;

import java.util.Collections;
import java.util.List;

public class Function {

    private final boolean mConstructor;
    private final String mId;
    private final Type mResultType;
    private final List<Pair<String, Type>> mParams;
    private final boolean mVarArg;
    private final Type mMember;
    private FunctionImpl mImpl;

    public Function(
            boolean constructor,
            String id,
            Type resultType,
            List<Pair<String, Type>> params,
            boolean vararg,
            Type member,
            FunctionImpl impl) {
        mConstructor = constructor;
        mId = id;
        mResultType = resultType;
        mParams = params;
        mVarArg = vararg;
        mMember = member;
        mImpl = impl;
    }

    public boolean isConstructor() {
        return mConstructor;
    }

    public String getId() {
        return mId;
    }

    public Type getResultType() {
        return mResultType;
    }

    public List<Pair<String, Type>> getParams() {
        return Collections.unmodifiableList(mParams);
    }

    public boolean isVarArg() {
        return mVarArg;
    }

    public Type getMember() {
        return mMember;
    }

    public FunctionImpl getImpl() {
        return mImpl;
    }

    public boolean isOpaque() {
        return mImpl == null;
    }

    public boolean setImpl(FunctionImpl impl) {
        if (isOpaque())
            return false;

        mImpl = impl;
        return true;
    }

    public boolean similar(String id, List<Pair<String, Type>> params, boolean vararg, Type member) {
        return mId.equals(id) &&
                mParams.equals(params) &&
                (mVarArg == vararg) &&
                (mMember == member || mMember.equals(member));
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

        return (mConstructor == func.mConstructor) &&
                mId.equals(func.mId) &&
                (mResultType == func.mResultType || (mResultType != null && mResultType.equals(func.mResultType))) &&
                mParams.equals(func.mParams) &&
                (mVarArg == func.mVarArg) &&
                mMember.equals(func.mMember) &&
                mImpl.equals(func.mImpl);
    }

    public static Function get(
            Environment env,
            boolean constructor,
            String id,
            Type resultType,
            List<Pair<String, Type>> params,
            boolean vararg,
            Type member,
            FunctionImpl impl) {

        var func = env.getFunction(id, params, vararg, member);
        if (func != null) {
            if (impl == null || func.setImpl(impl))
                return func;

            return null;
        }

        func = new Function(constructor, id, resultType, params, vararg, member, impl);

        return env.addFunction(func);
    }

}
