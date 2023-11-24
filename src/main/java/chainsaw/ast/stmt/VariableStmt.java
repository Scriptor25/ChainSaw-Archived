package chainsaw.ast.stmt;

import chainsaw.ast.expr.Expr;

public class VariableStmt extends Stmt {

    public String type;
    public String name;
    public Expr value;
}
