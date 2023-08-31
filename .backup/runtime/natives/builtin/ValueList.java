package io.scriptor.chainsaw.runtime.natives.builtin;

import java.util.List;
import java.util.Vector;

import io.scriptor.chainsaw.runtime.natives.CSawFunction;
import io.scriptor.chainsaw.runtime.natives.CSawParam;
import io.scriptor.chainsaw.runtime.natives.CSawType;
import io.scriptor.chainsaw.runtime.value.Value;

@CSawType(alias = "list")
public class ValueList {

    @CSawFunction
    public static ValueList list() {
        return new ValueList();
    }

    private List<Value> list = new Vector<>();

    @CSawFunction
    public void add(@CSawParam Value t) {
        list.add(t);
    }

    @CSawFunction
    public Value get(@CSawParam double idx) {
        return list.get((int)idx);
    }

    @CSawFunction
    public long size() {
        return list.size();
    }
}
