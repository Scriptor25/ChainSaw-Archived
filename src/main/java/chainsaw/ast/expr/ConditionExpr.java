package chainsaw.ast.expr;

public class ConditionExpr extends Expr {

    public Expr condition;
    public Expr thenExpr;
    public Expr elseExpr;

    public ConditionExpr(Expr condition, Expr thenExpr, Expr elseExpr) {
        this.condition = condition;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }
}
