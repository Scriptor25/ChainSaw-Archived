package io.scriptor.chainsaw.ast.stmt;

import io.scriptor.chainsaw.ast.expr.Expr;

public class ForStmt extends Stmt {

    public Stmt before;
    public Expr condition;
    public Stmt loop;
    public Stmt body;
}
