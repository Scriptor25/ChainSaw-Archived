package io.scriptor.chainsaw.runtime.function;

import io.scriptor.chainsaw.runtime.type.Type;

public class FuncParam {

    public String id;
    public Type type;

    public FuncParam(String id, Type type) {
        this.id = id;
        this.type = type;
    }

}
