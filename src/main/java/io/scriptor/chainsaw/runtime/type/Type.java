package io.scriptor.chainsaw.runtime.type;

import io.scriptor.chainsaw.Util;
import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.*;

public abstract class Type {

    protected final Environment mEnv;

    protected Type(Environment env) {
        mEnv = env;
    }

    public abstract Value nullValue();

    public boolean isVoid() {
        return this instanceof VoidType;
    }

    public boolean isNumber() {
        return this instanceof NumberType;
    }

    public boolean isChar() {
        return this instanceof CharType;
    }

    public boolean isString() {
        return this instanceof StringType;
    }

    public boolean isThing() {
        return this instanceof ThingType;
    }

    public boolean isNative() {
        return this instanceof NativeType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (!(o instanceof Type))
            return false;
        return this.isVoid() || ((Type) o).isVoid();
    }

    public static Type parseType(Environment env, String str) {
        if (str == null)
            return null;

        switch (str) {
            case "void":
                return VoidType.get(env);
            case "number":
                return NumberType.get(env);
            case "char":
                return CharType.get(env);
            case "string":
                return StringType.get(env);
        }

        Type type = ThingType.get(env, str);
        if (type != null)
            return type;

        type = NativeType.get(env, str);
        if (type != null)
            return type;

        return Util.error("undefined type '%s'", str);
    }

    public static Type parseType(Environment env, Class<?> cls) {
        if (cls == null)
            return null;

        if (cls.equals(CharValue.class) ||
                cls.equals(Character.class) ||
                cls.equals(char.class))
            return CharType.get(env);

        if (cls.equals(NumberValue.class) ||
                cls.equals(Boolean.class) ||
                Number.class.isAssignableFrom(cls) ||
                cls.isPrimitive())
            return NumberType.get(env);

        if (cls.equals(StringValue.class) ||
                CharSequence.class.isAssignableFrom(cls))
            return StringType.get(env);

        if (cls.equals(NativeValue.class))
            return NativeType.get(env, cls.getTypeParameters()[0].getGenericDeclaration());

        if (cls.equals(VoidValue.class) ||
                cls.equals(Void.class) ||
                cls.equals(void.class) ||
                cls.equals(Object.class))
            return VoidType.get(env);

        final var type = NativeType.get(env, cls);
        if (type != null)
            return type;

        return VoidType.get(env);
    }

}
