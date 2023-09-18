package io.scriptor.chainsaw.lang;

import io.scriptor.chainsaw.runtime.natives.CSawNative;
import io.scriptor.chainsaw.runtime.natives.CSawOverride;
import io.scriptor.chainsaw.runtime.value.NumberValue;
import io.scriptor.chainsaw.runtime.value.StringValue;

@CSawNative(alias = "string")
public class StdString {

    private StdString() {
    }

    @CSawOverride
    public static int length(StringValue value) {
        return value.getValue().length();
    }

    @CSawOverride
    public static char at(StringValue value, NumberValue index) {
        return value.getValue().charAt((int) (double) index.getValue());
    }
}
