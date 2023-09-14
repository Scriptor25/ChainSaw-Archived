package io.scriptor.chainsaw.ast.expr;

public class IdentifierExpr extends Expr {

    public String value;

    public IdentifierExpr(String value) {
        this.value = value;
    }
}