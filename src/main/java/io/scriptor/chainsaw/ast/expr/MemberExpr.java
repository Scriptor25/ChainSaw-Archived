package io.scriptor.chainsaw.ast.expr;

public class MemberExpr extends Expr {

    public Expr thing;
    public Expr member;

    public boolean isHead() {
        return !(member instanceof MemberExpr);
    }
}
