package chainsaw.ast.stmt;

import chainsaw.ast.expr.Expr;

public class ForStmt extends Stmt {

    public Stmt entry;
    public Expr condition;
    public Stmt next;
    public Stmt body;
}
