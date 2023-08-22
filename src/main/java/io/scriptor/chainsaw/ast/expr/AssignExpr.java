package io.scriptor.chainsaw.ast.expr;

public class AssignExpr extends Expr {

    public Expr assigne;
    public Expr value;

    public AssignExpr(Expr assigne, Expr value) {
        this.assigne = assigne;
        this.value = value;
    }
}
