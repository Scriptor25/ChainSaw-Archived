package io.scriptor.chainsaw.ast.stmt;

import io.scriptor.chainsaw.ast.expr.Expr;

public class WhileStmt extends Stmt {

    public Expr condition;
    public Stmt body;
}
