package io.scriptor.chainsaw.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.scriptor.chainsaw.runtime.NativeFuncBody.NativeFunc;
import io.scriptor.chainsaw.runtime.type.*;
import io.scriptor.chainsaw.runtime.value.*;

import static io.scriptor.chainsaw.Constants.*;

public class Environment {

    private Environment parent = null;
    private List<Function> functions = new Vector<>();
    private List<Type> types = new Vector<>();
    private Map<String, Value> values = new HashMap<>();

    public Environment() {
        registerStandardFunctions();
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void reset() {
        functions.clear();
        types.clear();
        values.clear();

        registerStandardFunctions();
    }

    private void registerStandardFunctions() {
        NativeType.create(this, FileStream.class, "file");

        { // sqrt
            List<FuncParam> params = new Vector<>();
            params.add(new FuncParam("t", NumberType.get(this)));

            registerNativeFunction("sqrt", NumberType.get(this), params, false, false,
                    args -> new NumberValue(this, Math.sqrt((double) args.get("t").getValue())));
        }

        { // floor
            List<FuncParam> params = new Vector<>();
            params.add(new FuncParam("t", NumberType.get(this)));

            registerNativeFunction("floor", NumberType.get(this), params, false, false,
                    args -> new NumberValue(this, Math.floor((double) args.get("t").getValue())));
        }

        { // out
            List<FuncParam> params = new Vector<>();
            params.add(new FuncParam("msg", StringType.get(this)));

            registerNativeFunction("out", VoidType.get(this), params, true, false,
                    args -> {
                        String msg = ((StringValue) args.get("msg")).getValue();

                        Object[] arg = new Object[args.size() - 1];
                        for (int i = 0; i < arg.length; i++)
                            arg[i] = args.get("vararg" + i);

                        System.out.print(Util.format(msg, arg));

                        return null;
                    });
        }

        { // fopen
            List<FuncParam> params = new Vector<>();
            params.add(new FuncParam("path", StringType.get(this)));
            params.add(new FuncParam("mode", StringType.get(this)));

            registerNativeFunction("fopen", NativeType.get(this, "file"), params, false, false,
                    args -> new NativeValue<>(this, new FileStream(
                            (String) args.get("path").getValue(),
                            (String) args.get("mode").getValue())));
        }

        { // fout
            List<FuncParam> params = new Vector<>();
            params.add(new FuncParam("file", NativeType.get(this, "file")));
            params.add(new FuncParam("msg", StringType.get(this)));

            registerNativeFunction("fout", VoidType.get(this), params, true, false,
                    args -> {
                        var file = (FileStream) args.get("file").getValue();
                        var msg = (String) args.get("msg").getValue();

                        Object[] arg = new Object[args.size() - 2];
                        for (int i = 0; i < arg.length; i++)
                            arg[i] = args.get("vararg" + i);

                        file.out(Util.format(msg, arg));

                        return null;
                    });
        }

        { // fin
            List<FuncParam> params = new Vector<>();
            params.add(new FuncParam("file", NativeType.get(this, "file")));

            registerNativeFunction("fin", StringType.get(this), params, false, false,
                    args -> new StringValue(this, ((FileStream) args.get("file").getValue()).in()));
        }

        { // fclose
            List<FuncParam> params = new Vector<>();
            params.add(new FuncParam("file", NativeType.get(this, "file")));

            registerNativeFunction("fclose", VoidType.get(this), params, false, false,
                    args -> {
                        var file = (FileStream) args.get("file").getValue();
                        file.close();

                        return null;
                    });
        }
    }

    public Environment getParent() {
        return parent;
    }

    public void createValue(String id, Value value) {
        if (values.containsKey(id))
            Util.error("value with id '%s' already defined", id);

        values.put(id, value);
    }

    public Value getValue(String id) {
        if (values.containsKey(id))
            return values.get(id);

        if (parent != null)
            return parent.getValue(id);

        return Util.error("no value with id '%s' defined", id);
    }

    public <V extends Value> V setValue(String id, V value) {
        if (values.containsKey(id)) {
            if (values.get(id) != null && !values.get(id).getType().isCompat(value.getType()))
                return Util.error("cannot assign value of type %s to '%s'", value.getType(), id);

            values.put(id, value);
            return value;
        }

        if (parent != null)
            return parent.setValue(id, value);

        return Util.error("no value with id '%s' defined", id);
    }

    public Function addFunction(Function func) {
        if (functions.contains(func))
            return Util.error("function %s already existing", func);

        functions.add(func);
        return func;
    }

    public Function getFunction(FuncType type, String id) {
        for (Function func : functions)
            if (func.equals(type, id))
                return func;

        if (parent != null)
            return parent.getFunction(type, id);

        return null;
    }

    public void registerNativeFunction(
            String id,
            Type result,
            List<FuncParam> params,
            boolean vararg,
            boolean constructor,
            NativeFunc func) {

        var function = Function.get(this,
                FuncType.get(
                        this,
                        result,
                        params,
                        vararg),
                id, constructor);

        function.setImpl(new NativeFuncBody(function, func));
    }

    public Function getFunctionByRef(String id, List<Value> args) {
        for (Function func : functions) {
            if (!func.getId().equals(id))
                continue;

            var type = func.getType();
            var params = type.getParams();
            if (params.size() != args.size() && !type.isVararg())
                continue;

            int arg = 0;
            for (; arg < params.size(); arg++)
                if (!params.get(arg).type.isCompat(args.get(arg).getType()))
                    break;

            if (arg != params.size())
                continue;

            return func;
        }

        if (parent != null)
            return parent.getFunctionByRef(id, args);

        return null;
    }

    public <T extends Type> T addType(T type) {
        if (types.contains(type))
            return Util.error("type %s already existing", type);

        types.add(type);
        return type;
    }

    public VoidType getVoidType() {
        for (Type type : types)
            if (type instanceof VoidType)
                return (VoidType) type;

        if (parent != null)
            return parent.getVoidType();

        return null;
    }

    public NumberType getNumberType() {
        for (Type type : types)
            if (type instanceof NumberType)
                return (NumberType) type;

        if (parent != null)
            return parent.getNumberType();

        return null;
    }

    public StringType getStringType() {
        for (Type type : types)
            if (type instanceof StringType)
                return (StringType) type;

        if (parent != null)
            return parent.getStringType();

        return null;
    }

    public ThingType getThingType(String id) {
        for (Type type : types)
            if (type instanceof ThingType) {
                ThingType tt = (ThingType) type;
                if (tt.equals(id))
                    return tt;
            }

        if (parent != null)
            return parent.getThingType(id);

        return null;
    }

    public FuncType getFuncType(Type result, List<FuncParam> params, boolean vararg) {
        for (Type type : types)
            if (type instanceof FuncType) {
                FuncType ft = (FuncType) type;
                if (ft.equals(result, params, vararg))
                    return ft;
            }

        if (parent != null)
            return parent.getFuncType(result, params, vararg);

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> NativeType<T> getNativeType(Class<T> nativeClass, String alias) {
        for (Type type : types)
            if (type instanceof NativeType) {
                NativeType<?> nt = (NativeType<?>) type;
                if (nt.equals(nativeClass, alias))
                    return (NativeType<T>) nt;
            }

        if (parent != null)
            return parent.getNativeType(nativeClass, alias);

        return null;
    }

    @Override
    public String toString() {
        return PGSON.toJson(this);
    }

}
