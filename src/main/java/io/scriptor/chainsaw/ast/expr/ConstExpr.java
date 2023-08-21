package io.scriptor.chainsaw.ast.expr;

public class ConstExpr extends Expr {

    public String value;
    public ConstType type;

    public ConstExpr(String value, ConstType type) {
        this.value = value;
        this.type = type;
    }
}
