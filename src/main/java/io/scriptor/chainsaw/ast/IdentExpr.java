package io.scriptor.chainsaw.ast;

public class IdentExpr extends Expr {

    public String value;

    public IdentExpr(String ident) {
        this.value = ident;
    }
}