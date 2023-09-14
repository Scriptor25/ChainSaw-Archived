package io.scriptor.chainsaw.ast.stmt;

import io.scriptor.chainsaw.ast.expr.Expr;

public class ReturnStmt extends Stmt {

    public Expr value;

    public ReturnStmt(Expr value) {
        this.value = value;
    }
}
