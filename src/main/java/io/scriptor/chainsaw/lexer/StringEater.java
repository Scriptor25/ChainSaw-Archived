package io.scriptor.chainsaw.lexer;

public class StringEater {

    private final String mValue;
    private int mIndex = 0;

    public StringEater(String value) {
        this.mValue = value;
    }

    public void reset() {
        mIndex = 0;
    }

    public char next() {
        return next(0);
    }

    public char next(int off) {
        if (!ok(off))
            return (char) 0;
        return mValue.charAt(mIndex + off);
    }

    public char eat() {
        if (!ok())
            return (char) 0;
        return mValue.charAt(mIndex++);
    }

    public boolean ok() {
        return ok(0);
    }

    public boolean ok(int off) {
        return (mIndex + off) < mValue.length();
    }

}
