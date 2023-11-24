package chainsaw.ast.expr;

public class AssignmentExpr extends Expr {

    public Expr assignee;
    public Expr value;

    public AssignmentExpr(Expr assignee, Expr value) {
        this.assignee = assignee;
        this.value = value;
    }
}
