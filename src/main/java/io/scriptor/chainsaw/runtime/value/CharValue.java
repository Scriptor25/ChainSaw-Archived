package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.CharType;

public class CharValue extends Value {

    private char mValue;

    public CharValue(Environment env, char value) {
        super(CharType.get(env));
        mValue = value;
    }

    @Override
    public Character getValue() {
        return mValue;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && ((CharValue) other).mValue == mValue;
    }

}
