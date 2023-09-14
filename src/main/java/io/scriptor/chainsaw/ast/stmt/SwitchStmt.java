package io.scriptor.chainsaw.ast.stmt;

import io.scriptor.chainsaw.ast.expr.Expr;

import java.util.HashMap;
import java.util.Map;

public class SwitchStmt extends Stmt {

    public Expr condition;
    public Map<Expr, Stmt> cases = new HashMap<>();
    public Stmt defaultCase;
}
