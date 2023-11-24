package chainsaw.ast.stmt;

import chainsaw.ast.expr.Expr;

public class WhileStmt extends Stmt {

    public Expr condition;
    public Stmt body;
}
