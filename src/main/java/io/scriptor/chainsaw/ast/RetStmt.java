package io.scriptor.chainsaw.ast;

public class RetStmt extends Stmt {

    public Expr value;

    public RetStmt(Expr value) {
        this.value = value;
    }
}
