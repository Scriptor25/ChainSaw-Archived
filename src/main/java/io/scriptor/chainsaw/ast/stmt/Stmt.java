package io.scriptor.chainsaw.ast.stmt;

import static io.scriptor.chainsaw.Constants.PGSON;

public abstract class Stmt {

    @Override
    public String toString() {
        var json = PGSON.toJsonTree(this);
        json.getAsJsonObject().addProperty("class", getClass().getSimpleName());
        return PGSON.toJson(json);
    }
}
