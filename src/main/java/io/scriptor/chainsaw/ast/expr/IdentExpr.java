package io.scriptor.chainsaw.ast.expr;

public class IdentExpr extends Expr {

    public String value;

    public IdentExpr(String ident) {
        this.value = ident;
    }
}