package io.scriptor.chainsaw.runtime;

import io.scriptor.chainsaw.runtime.type.Type;

public class FuncParam {

    public String id;
    public Type type;

    public FuncParam(String id, Type type) {
        this.id = id;
        this.type = type;
    }
}
