package chainsaw.runtime.value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import chainsaw.Util;
import chainsaw.runtime.type.ThingType;

public class ThingValue extends Value {

    private final Map<String, Value> mFields = new HashMap<>();

    public ThingValue(ThingType type) {
        super(type);
        for (var field : type.getFields().keySet())
            mFields.put(field, type.getFields().get(field).nullValue());
    }

    public boolean hasField(String field) {
        return ((ThingType) mType).hasField(field);
    }

    @SuppressWarnings("unchecked")
    public <V extends Value> V getField(String field) {
        if (!hasField(field))
            return Util.error("Thing doesn't have field '%s'", field);
        return (V) mFields.get(field);
    }

    @SuppressWarnings("unchecked")
    public <V extends Value> V setField(String field, V value) {
        if (!hasField(field))
            return Util.error("Thing doesn't have field '%s'", field);
        return (V) mFields.put(field, value);
    }

    @Override
    public Map<String, Value> getValue() {
        return Collections.unmodifiableMap(mFields);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ThingValue setValue(Object value) {
        mFields.putAll((Map<String, Value>) value);
        return this;
    }

}
