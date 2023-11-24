package chainsaw.runtime.function;

import chainsaw.ast.stmt.BodyStmt;

public class ASTImplementation implements FunctionImplementation {

    private final BodyStmt mBody;

    public ASTImplementation(BodyStmt body) {
        mBody = body;
    }

    public BodyStmt getBody() {
        return mBody;
    }
}
