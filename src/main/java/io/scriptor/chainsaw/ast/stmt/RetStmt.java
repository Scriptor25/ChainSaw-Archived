package io.scriptor.chainsaw.ast.stmt;

import io.scriptor.chainsaw.ast.expr.Expr;

public class RetStmt extends Stmt {

    public Expr value;

    public RetStmt(Expr value) {
        this.value = value;
    }
}
