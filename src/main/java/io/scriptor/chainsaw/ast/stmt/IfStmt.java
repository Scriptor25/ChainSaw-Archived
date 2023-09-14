package io.scriptor.chainsaw.ast.stmt;

import io.scriptor.chainsaw.ast.expr.Expr;

public class IfStmt extends Stmt {

    public Expr condition;
    public Stmt thenStmt;
    public Stmt elseStmt;
}
