package io.scriptor.chainsaw.ast.stmt;

import com.google.gson.GsonBuilder;

public abstract class Stmt {

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
    }
}
