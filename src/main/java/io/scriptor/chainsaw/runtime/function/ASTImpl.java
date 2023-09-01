package io.scriptor.chainsaw.runtime.function;

import io.scriptor.chainsaw.ast.stmt.BodyStmt;

public class ASTImpl implements FunctionImpl {

    private final BodyStmt mBody;

    public ASTImpl(BodyStmt body) {
        mBody = body;
    }

    public BodyStmt getBody() {
        return mBody;
    }
}
