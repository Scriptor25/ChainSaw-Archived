package io.scriptor.chainsaw.runtime.nativeclass;

import java.util.List;
import java.util.Vector;

import io.scriptor.chainsaw.runtime.value.Value;

public class ValueList {

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
