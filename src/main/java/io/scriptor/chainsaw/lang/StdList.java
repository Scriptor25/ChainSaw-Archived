package io.scriptor.chainsaw.lang;

import java.util.Vector;

import io.scriptor.chainsaw.runtime.natives.CSawNative;
import io.scriptor.chainsaw.runtime.value.Value;

@CSawNative(alias = "list")
public class StdList extends Vector<Value> {
}