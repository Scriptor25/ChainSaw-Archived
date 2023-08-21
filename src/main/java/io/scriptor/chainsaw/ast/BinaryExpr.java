package io.scriptor.chainsaw.ast;

public class BinaryExpr extends Expr {

    public Expr left;
    public Expr right;
    public String operator;

    public BinaryExpr(Expr left, Expr right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }
}
