package chainsaw.ast.expr;

public class UnaryExpr extends Expr {

    public String operator;
    public Expr value;

    public UnaryExpr(String operator, Expr value) {
        this.operator = operator;
        this.value = value;
    }
}
