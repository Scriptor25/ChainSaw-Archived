package io.scriptor.chainsaw.ast;

public class IfStmt extends Stmt {

    public Expr condition;
    public Stmt isTrue;
    public Stmt isFalse;
}
