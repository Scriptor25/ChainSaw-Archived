package chainsaw.lang;

import java.util.Vector;

import chainsaw.runtime.natives.CSawNative;
import chainsaw.runtime.value.Value;

@CSawNative(alias = "list")
public class StdList extends Vector<Value> {
}
