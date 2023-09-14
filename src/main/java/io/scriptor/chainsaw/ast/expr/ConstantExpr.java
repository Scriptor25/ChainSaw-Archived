package io.scriptor.chainsaw.ast.expr;

public class ConstantExpr extends Expr {

    public enum ConstantType {
        NUMBER,
        STRING,
        CHAR
    }

    public String value;
    public ConstantType type;

    public ConstantExpr(String value, ConstantType type) {
        this.value = value;
        this.type = type;
    }
}
