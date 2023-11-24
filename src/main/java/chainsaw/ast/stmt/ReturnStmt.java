package chainsaw.ast.stmt;

import chainsaw.ast.expr.Expr;

public class ReturnStmt extends Stmt {

    public Expr value;

    public ReturnStmt(Expr value) {
        this.value = value;
    }
}
