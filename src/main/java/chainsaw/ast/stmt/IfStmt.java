package chainsaw.ast.stmt;

import chainsaw.ast.expr.Expr;

public class IfStmt extends Stmt {

    public Expr condition;
    public Stmt thenStmt;
    public Stmt elseStmt;
}
