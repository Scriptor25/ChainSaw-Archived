package io.scriptor.chainsaw.runtime.type;

import java.util.Collections;
import java.util.Map;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.value.ThingValue;
import io.scriptor.chainsaw.runtime.value.Value;

public class ThingType extends Type {

    private String id;
    private Map<String, Type> fields;

    private ThingType(Environment env) {
        super(env);
    }

    public String getId() {
        return id;
    }

    public Map<String, Type> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public boolean hasField(String id) {
        return fields.containsKey(id);
    }

    public boolean hasField(String id, Type type) {
        return fields.containsKey(id) && fields.get(id).isCompat(type);
    }

    public boolean isOpaque() {
        return fields == null;
    }

    public boolean equals(String id) {
        return this.id.equals(id);
    }

    @Override
    public Value nullValue() {
        return new ThingValue(environment, this);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && equals(((ThingType) o).id);
    }

    public static ThingType get(Environment env, String id) {
        var type = env.getThingType(id);
        return type;
    }

    public static ThingType create(Environment env, String id, Map<String, Type> fields) {
        var type = get(env, id);
        if (type != null && !type.isOpaque())
            return null;

        if (type == null) {
            type = new ThingType(env);
            type.id = id;
            env.addType(type);
        }

        type.fields = fields;

        return type;
    }
}
