package io.scriptor.chainsaw.runtime;

import java.util.Collections;
import java.util.Map;

public class ThingType extends Type {

    private String mId;
    private Map<String, Type> mFields;

    private ThingType() {
    }

    public String getId() {
        return mId;
    }

    public Map<String, Type> getFields() {
        return Collections.unmodifiableMap(mFields);
    }

    public boolean isOpaque() {
        return mFields == null;
    }

    public static ThingType get(Environment env, String id) {
        var thing = env.getThingType(id);
        if (thing != null)
            return thing;

        thing = new ThingType();
        thing.mId = id;
        env.addThingType(thing);

        return thing;
    }

    public static ThingType get(Environment env, String id, Map<String, Type> fields) {
        var thing = get(env, id);
        if (thing != null && !thing.isOpaque())
            return Util.error("thing '%s' already was a thing...", id);

        thing.mFields = fields;

        return thing;
    }
}
