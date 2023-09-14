package io.scriptor.chainsaw.runtime.function;

import io.scriptor.chainsaw.ast.stmt.BodyStmt;

public class ASTImplementation implements FunctionImplementation {

    private final BodyStmt mBody;

    public ASTImplementation(BodyStmt body) {
        mBody = body;
    }

    public BodyStmt getBody() {
        return mBody;
    }
}
