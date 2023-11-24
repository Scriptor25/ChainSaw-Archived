package chainsaw.runtime.value;

import chainsaw.runtime.Environment;
import chainsaw.runtime.type.CharType;

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
    public CharValue setValue(Object value) {
        mValue = (char) value;
        return this;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && ((CharValue) other).mValue == mValue;
    }

}
