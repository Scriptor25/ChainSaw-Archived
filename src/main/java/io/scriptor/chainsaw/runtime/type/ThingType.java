package io.scriptor.chainsaw.runtime.type;

import java.util.Collections;
import java.util.Map;

import io.scriptor.chainsaw.runtime.Environment;

public class ThingType extends Type {

    private String id;
    private Map<String, Type> fields;

    private ThingType(Environment env) {
        super(env, "ThingType");
    }

    public String getId() {
        return id;
    }

    public Map<String, Type> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public boolean isOpaque() {
        return fields == null;
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
        }

        type.fields = fields;

        return env.addType(type);
    }

    public boolean equals(String id) {
        return this.id.equals(id);
    }
}
