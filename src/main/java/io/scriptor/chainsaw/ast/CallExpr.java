package io.scriptor.chainsaw.ast;

import java.util.List;
import java.util.Vector;

public class CallExpr extends Expr {

    public String function;
    public List<Expr> args = new Vector<>();
}
