package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.StringType;

public class StringValue extends Value {

    private String value;

    public StringValue(Environment env, String value) {
        super(env, StringType.get(env));
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
