package io.scriptor.chainsaw.runtime.value;

import io.scriptor.chainsaw.runtime.type.Type;
import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.Error;

public abstract class Value {

    protected final Type mType;

    protected Value(Type type) {
        mType = type;
    }

    public Type getType() {
        return mType;
    }

    public abstract Object getValue();

    public boolean isVoid() {
        return getType().isVoid();
    }

    public boolean isNumber() {
        return getType().isNumber();
    }

    public boolean isChar() {
        return getType().isChar();
    }

    public boolean isString() {
        return getType().isString();
    }

    public boolean isThing() {
        return getType().isThing();
    }

    public boolean isNative() {
        return getType().isNative();
    }

    @Override
    public String toString() {
        if (getValue() == null)
            return "null";
        return getValue().toString();
    }

    @Override
    public boolean equals(Object other) {
        return other != null && this.getClass().isInstance(other) || other == this;
    }

    public static Value extract(Value value) {
        if (value == null)
            return null;
        if (value.getValue() instanceof Value)
            return extract((Value) value.getValue());

        return value;
    }

    public static Type[] toTypeArray(Value[] values) {
        Type[] types = new Type[values.length];

        for (int i = 0; i < values.length; i++)
            types[i] = values[i].mType;

        return types;
    }

    public static boolean asBoolean(Value value) {
        if (value == null || value.getValue() == null)
            return false;

        if (value instanceof NumberValue) {
            if (((NumberValue) value).getValue() == 0)
                return false;
        } else if (value instanceof CharValue) {
            if (((CharValue) value).getValue() == 0)
                return false;
        }

        return true;
    }

    public static boolean equals(Environment env, Value left, Value right) {
        return left.equals(right);
    }

    public static boolean less(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return ((NumberValue) left).getValue() < ((NumberValue) right).getValue();

        return Error.error("operator '<' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static boolean greater(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return ((NumberValue) left).getValue() > ((NumberValue) right).getValue();

        return Error.error("operator '>' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static boolean lessEq(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return ((NumberValue) left).getValue() <= ((NumberValue) right).getValue();

        return Error.error("operator '<=' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static boolean greaterEq(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return ((NumberValue) left).getValue() >= ((NumberValue) right).getValue();

        return Error.error("operator '>=' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static Value add(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return new NumberValue(env, ((NumberValue) left).getValue() + ((NumberValue) right).getValue());
        if (left.isString() || right.isString())
            return new StringValue(env, left.toString() + right.toString());

        return Error.error("operator '+' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static Value sub(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return new NumberValue(env, ((NumberValue) left).getValue() - ((NumberValue) right).getValue());

        return Error.error("operator '-' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static Value mul(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return new NumberValue(env, ((NumberValue) left).getValue() * ((NumberValue) right).getValue());

        return Error.error("operator '*' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static Value div(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return new NumberValue(env, ((NumberValue) left).getValue() / ((NumberValue) right).getValue());

        return Error.error("operator '/' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }
}
