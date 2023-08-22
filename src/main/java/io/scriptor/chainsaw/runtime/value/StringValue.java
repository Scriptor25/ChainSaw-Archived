package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.ThingType;

public class StringValue extends Value {

    private String value;

    public StringValue(Environment env, ThingType type, String value) {
        super(env, type);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
