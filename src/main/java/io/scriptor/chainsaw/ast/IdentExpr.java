package io.scriptor.chainsaw.ast;

public class IdentExpr extends Expr {

    public String ident;

    public IdentExpr(String ident) {
        this.ident = ident;
    }
}