package io.scriptor.chainsaw.runtime.value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.Util;
import io.scriptor.chainsaw.runtime.type.ThingType;

public class ThingValue extends Value {

    private Map<String, Value> fields;

    public ThingValue(Environment env, ThingType type) {
        this(env, type, new HashMap<>());
    }

    public ThingValue(Environment env, ThingType type, Map<String, Value> fields) {
        super(env, type);
        this.fields = fields;
    }

    public Value getField(String id) {
        if (!((ThingType) type).hasField(id))
            return Util.error("undefined field '%s'", id);

        return fields.get(id);
    }

    public Value setField(String id, Value value) {
        if (!((ThingType) type).hasField(id, value.getType()))
            return Util.error("undefined field '%s'", id);

        fields.put(id, value);

        return value;
    }

    @Override
    public Map<String, Value> getValue() {
        return Collections.unmodifiableMap(fields);
    }

}
