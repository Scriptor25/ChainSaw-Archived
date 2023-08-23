package io.scriptor.chainsaw.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.scriptor.chainsaw.runtime.type.*;
import io.scriptor.chainsaw.runtime.value.*;

import static io.scriptor.chainsaw.Constants.*;

public class Environment {

    private Environment parent = null;
    private List<Function> functions = new Vector<>();
    private List<Type> types = new Vector<>();
    private Map<String, Value> values = new HashMap<>();

    public Environment() {
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void reset() {
        functions.clear();
        types.clear();
        values.clear();
    }

    public Environment getParent() {
        return parent;
    }

    public <V extends Value> V createValue(String id, V value) {
        if (values.containsKey(id))
            return Util.error("value with id '%s' already defined", id);

        values.put(id, value);

        return value;
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

    public NumberType getNumberType(int bits) {
        for (Type type : types)
            if (type instanceof NumberType) {
                NumberType ft = (NumberType) type;
                if (ft.equals(bits))
                    return ft;
            }

        if (parent != null)
            return parent.getNumberType(bits);

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

    @Override
    public String toString() {
        return PGSON.toJson(this);
    }

}
