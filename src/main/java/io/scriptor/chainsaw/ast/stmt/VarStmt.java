package io.scriptor.chainsaw.ast.stmt;

import io.scriptor.chainsaw.ast.expr.Expr;

public class VarStmt extends Stmt {

    public String type;
    public String ident;
    public Expr value;
}