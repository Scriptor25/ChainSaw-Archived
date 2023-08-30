package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.StringType;

public class StringValue extends Value {

    private CharSequence value;

    public StringValue(Environment env, CharSequence value) {
        super(env, StringType.get(env));
        this.value = value;
    }

    @Override
    public CharSequence getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
