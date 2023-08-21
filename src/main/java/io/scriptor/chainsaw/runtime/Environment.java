package io.scriptor.chainsaw.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import static io.scriptor.chainsaw.Constants.*;

public class Environment {

    private List<Function> mFunctions = new Vector<>();
    private List<ThingType> mThings = new Vector<>();

    public void reset() {
        mFunctions.clear();
        mThings.clear();
    }

    public List<ThingType> getThingTypes() {
        return Collections.unmodifiableList(mThings);
    }

    public void addThingType(ThingType thing) {
        if (thing == null || thing.getId() == null)
            return;
        mThings.add(thing);
    }

    public ThingType getThingType(String id) {
        for (var thing : mThings)
            if (thing.getId().equals(id))
                return thing;

        return null;
    }

    public Type getIntType() {
        return null;
    }

    public Type getFloatType() {
        return null;
    }

    @Override
    public String toString() {
        return PGSON.toJson(this);
    }

}
