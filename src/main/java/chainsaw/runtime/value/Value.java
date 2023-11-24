package chainsaw.runtime.value;

import java.util.List;
import java.util.Vector;

import chainsaw.Util;
import chainsaw.runtime.Environment;
import chainsaw.runtime.type.Type;

public abstract class Value {

    protected final Type mType;

    protected Value(Type type) {
        mType = type;
    }

    public Type getType() {
        return mType;
    }

    public abstract Object getValue();

    public abstract Value setValue(Object value);

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

    public boolean isReturn() {
        return this instanceof ReturnValue;
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

    public static Value fromObject(Environment env, Object object) {
        if (object == null)
            return null;

        if (object instanceof Value)
            return Value.extract((Value) object);

        var type = Type.parseType(env, object.getClass());
        var value = type.nullValue().setValue(object);

        return value;
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
            if ((double) value.getValue() == 0)
                return false;
        } else if (value instanceof CharValue) {
            if (((CharValue) value).getValue() == 0)
                return false;
        }

        return true;
    }

    public static Value cast(Environment env, Value value, Type type) {
        if (value == null || type == null)
            return null;

        if (type.isString())
            return new StringValue(env, value.toString());

        if (value.getType().isString()) {
            if (type.isNumber())
                return new NumberValue(env, Double.parseDouble((String) value.getValue()));

            if (type.isChar() && ((String) value.getValue()).length() == 1)
                return new CharValue(env, ((String) value.getValue()).charAt(0));
        }

        if (value.getType().isChar()) {
            if (type.isNumber())
                return new NumberValue(env, ((Character) value.getValue()));
        }

        return Util.error("cannot cast value of type %s to type %s",
                value.getType(),
                type);
    }

    public static boolean equals(Environment env, Value left, Value right) {
        return left.equals(right);
    }

    public static boolean less(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return (double) left.getValue() < (double) right.getValue();

        return Util.error("operator '<' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static boolean greater(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return (double) left.getValue() > (double) right.getValue();

        return Util.error("operator '>' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static boolean lessEq(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return (double) left.getValue() <= (double) right.getValue();

        return Util.error("operator '<=' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static boolean greaterEq(Environment env, Value left, Value right) {
        if (left.isNumber() && right.isNumber())
            return (double) left.getValue() >= (double) right.getValue();

        return Util.error("operator '>=' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static Value add(Environment env, Value left, Value right, boolean assigning) {
        if (left.isNumber() && right.isNumber())
            return new NumberValue(env, (double) left.getValue() + (double) right.getValue());
        if (left.isString() || right.isString())
            return new StringValue(env, left.toString() + right.toString());

        Value thing = null;
        String name = "+";
        List<Value> args = new Vector<>();
        if (assigning) {
            thing = left;
            name += "=";
            args.add(right);
        } else {
            args.add(left);
            args.add(right);
        }

        final var func = env.getFunction(thing, name, args.toArray(new Value[0]));
        if (func != null)
            return env.getInterpreter().evaluateFunction(thing, name, args.toArray(new Value[0]));

        return Util.error("operator '+' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static Value sub(Environment env, Value left, Value right, boolean assigning) {
        if (left.isNumber() && right.isNumber())
            return new NumberValue(env, (double) left.getValue() - (double) right.getValue());

        Value thing = null;
        String name = "-";
        List<Value> args = new Vector<>();
        if (assigning) {
            thing = left;
            name += "=";
            args.add(right);
        } else {
            args.add(left);
            args.add(right);
        }

        final var func = env.getFunction(thing, name, args.toArray(new Value[0]));
        if (func != null)
            return env.getInterpreter().evaluateFunction(thing, name, args.toArray(new Value[0]));

        return Util.error("operator '-' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static Value mul(Environment env, Value left, Value right, boolean assigning) {
        if (left.isNumber() && right.isNumber())
            return new NumberValue(env, (double) left.getValue() * (double) right.getValue());

        Value thing = null;
        String name = "*";
        List<Value> args = new Vector<>();
        if (assigning) {
            thing = left;
            name += "=";
            args.add(right);
        } else {
            args.add(left);
            args.add(right);
        }

        final var func = env.getFunction(thing, name, args.toArray(new Value[0]));
        if (func != null)
            return env.getInterpreter().evaluateFunction(thing, name, args.toArray(new Value[0]));

        return Util.error("operator '*' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static Value div(Environment env, Value left, Value right, boolean assigning) {
        if (left.isNumber() && right.isNumber())
            return new NumberValue(env, (double) left.getValue() / (double) right.getValue());

        Value thing = null;
        String name = "/";
        List<Value> args = new Vector<>();
        if (assigning) {
            thing = left;
            name += "=";
            args.add(right);
        } else {
            args.add(left);
            args.add(right);
        }

        final var func = env.getFunction(thing, name, args.toArray(new Value[0]));
        if (func != null)
            return env.getInterpreter().evaluateFunction(thing, name, args.toArray(new Value[0]));

        return Util.error("operator '/' not defined for types '%s' and '%s'", left.getType(), right.getType());
    }

    public static Value negate(Environment env, Value value) {
        if (value == null)
            return null;

        if (value.isNumber())
            return new NumberValue(env, -(double) value.getValue());

        final var func = env.getFunction(value, "-");
        if (func != null)
            return env.getInterpreter().evaluateFunction(value, "-");

        return Util.error("not yet implemented");
    }

    public static Value not(Environment env, Value value) {
        if (value.isNumber())
            return new NumberValue(env, !asBoolean(value) ? 1 : 0);

        final var func = env.getFunction(value, "!");
        if (func != null)
            return env.getInterpreter().evaluateFunction(value, "!");

        return Util.error("not yet implemented");
    }
}
