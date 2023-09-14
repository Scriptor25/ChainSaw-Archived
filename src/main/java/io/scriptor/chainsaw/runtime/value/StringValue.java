package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.StringType;

public class StringValue extends Value {

    private String mValue;

    public StringValue(Environment env, String value) {
        super(StringType.get(env));
        mValue = value;
    }

    @Override
    public String getValue() {
        return mValue;
    }

    @Override
    public StringValue setValue(Object value) {
        mValue = (String) value;
        return this;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) &&
                (((StringValue) other).mValue == mValue ||
                        (((StringValue) other).mValue != null && mValue != null &&
                                ((StringValue) other).mValue.equals(mValue)));
    }

}
