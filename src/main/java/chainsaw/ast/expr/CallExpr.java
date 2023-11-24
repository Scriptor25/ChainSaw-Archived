package chainsaw.ast.expr;

import java.util.List;
import java.util.Vector;

public class CallExpr extends Expr {

    public Expr function;
    public List<Expr> args = new Vector<>();
}
