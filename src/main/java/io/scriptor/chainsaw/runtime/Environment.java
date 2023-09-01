package io.scriptor.chainsaw.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {

    private final Environment mParent;
    private final Map<String, Value> mVariables = new HashMap<>();
    private final Map<String, List<Function>> mFunctions = new HashMap<>();

    public Environment() {
        mParent = null;
    }

    public Environment(Environment parent) {
        mParent = parent;
    }

    public Environment getParent() {
        return mParent;
    }

    public Value getVariable(String id) {
        if (!mVariables.containsKey(id))
            return mParent.getVariable(id);

        return mVariables.get(id);
    }

    public Function getFunction(String id, Value... args) {
        if (!mFunctions.containsKey(id))
            return mParent.getFunction(id, args);

        return mFunctions.get(id);
    }

}
