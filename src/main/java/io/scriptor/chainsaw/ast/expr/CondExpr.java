package io.scriptor.chainsaw.ast.expr;

public class CondExpr extends Expr {

    public Expr condition;
    public Expr isTrue;
    public Expr isFalse;

    public CondExpr(Expr condition, Expr isTrue, Expr isFalse) {
        this.condition = condition;
        this.isTrue = isTrue;
        this.isFalse = isFalse;
    }
}
