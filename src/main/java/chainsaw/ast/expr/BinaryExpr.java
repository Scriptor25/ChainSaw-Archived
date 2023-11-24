package chainsaw.ast.expr;

public class BinaryExpr extends Expr {

    public Expr left;
    public Expr right;
    public String operator;
    public boolean assigning;

    public BinaryExpr(Expr left, Expr right, String operator) {
        this(left, right, operator, false);
    }

    public BinaryExpr(Expr left, Expr right, String operator, boolean assigning) {
        this.left = left;
        this.right = right;
        this.operator = operator;
        this.assigning = assigning;
    }
}
