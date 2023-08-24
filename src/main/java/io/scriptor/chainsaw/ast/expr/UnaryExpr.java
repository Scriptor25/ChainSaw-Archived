package io.scriptor.chainsaw.ast.expr;

public class UnaryExpr extends Expr {

    public String operator;
    public Expr expr;

    public UnaryExpr(String operator, Expr expr) {
        this.operator = operator;
        this.expr = expr;
    }
}
