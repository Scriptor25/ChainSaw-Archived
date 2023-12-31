package chainsaw.runtime.type;

import java.util.Collections;
import java.util.Map;

import chainsaw.Util;
import chainsaw.runtime.Environment;
import chainsaw.runtime.value.ThingValue;
import chainsaw.runtime.value.Value;

public class ThingType extends Type {

    private final String mId;
    private Map<String, Type> mFields;

    private ThingType(Environment env, String id, Map<String, Type> fields) {
        super(env);
        mId = id;
        mFields = fields;
    }

    public String getId() {
        return mId;
    }

    public Map<String, Type> getFields() {
        return Collections.unmodifiableMap(mFields);
    }

    public boolean hasField(String field) {
        return mFields.containsKey(field);
    }

    public boolean isOpaque() {
        return mFields == null;
    }

    @Override
    public Value nullValue() {
        return new ThingValue(this);
    }

    @Override
    public String toString() {
        return mId;
    }

    public static ThingType create(Environment env, String id, Map<String, Type> fields) {
        var type = env.getThingType(id);
        if (type != null) {

            if (fields == null)
                return type;
            if (!type.isOpaque())
                return Util.error("Thing '%s' is not opaque", id);

            type.mFields = fields;
            return type;
        }

        type = new ThingType(env, id, fields);
        return env.addThingType(type);
    }

    public static ThingType get(Environment env, String id) {
        return env.getThingType(id);
    }

}
