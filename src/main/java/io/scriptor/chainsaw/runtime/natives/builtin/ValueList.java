package io.scriptor.chainsaw.runtime.natives.builtin;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.natives.CSawFunction;
import io.scriptor.chainsaw.runtime.natives.CSawType;
import io.scriptor.chainsaw.runtime.value.NativeValue;
import io.scriptor.chainsaw.runtime.value.NumberValue;
import io.scriptor.chainsaw.runtime.value.Value;

@CSawType(alias = "list")
public class ValueList {

    @CSawFunction(id = "list", constructor = true, result = "list")
    public static Value list(Environment env, Map<String, Value> params) {
        return new NativeValue<>(env, new ValueList());
    }

    @CSawFunction(id = "add", params = { "t: void" }, memberOf = "list")
    public static Value add(Environment env, Map<String, Value> params) {
        @SuppressWarnings("unchecked")
        var list = ((NativeValue<ValueList>) params.get("my")).getValue();

        list.add(params.get("t"));

        return null;
    }

    @CSawFunction(id = "size", result = "num", memberOf = "list")
    public static Value size(Environment env, Map<String, Value> params) {
        @SuppressWarnings("unchecked")
        var list = ((NativeValue<ValueList>) params.get("my")).getValue();

        return new NumberValue(env, list.size());
    }

    @CSawFunction(id = "get", result = "void", params = { "idx: num" }, memberOf = "list")
    public static Value get(Environment env, Map<String, Value> params) {
        @SuppressWarnings("unchecked")
        var list = ((NativeValue<ValueList>) params.get("my")).getValue();
        var idx = (double) ((NumberValue) params.get("idx")).getValue();

        return list.get((int) idx);
    }

    private List<Value> list = new Vector<>();

    public void add(Value t) {
        list.add(t);
    }

    public Value get(int idx) {
        return list.get(idx);
    }

    public long size() {
        return list.size();
    }
}
