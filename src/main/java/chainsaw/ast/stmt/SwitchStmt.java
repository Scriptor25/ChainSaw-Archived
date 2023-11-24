package chainsaw.ast.stmt;

import java.util.HashMap;
import java.util.Map;

import chainsaw.ast.expr.Expr;

public class SwitchStmt extends Stmt {

    public Expr condition;
    public Map<Expr, Stmt> cases = new HashMap<>();
    public Stmt defaultCase;
}
