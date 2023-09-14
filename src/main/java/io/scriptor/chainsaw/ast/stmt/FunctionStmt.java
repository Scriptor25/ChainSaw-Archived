package io.scriptor.chainsaw.ast.stmt;

import java.util.List;
import java.util.Vector;

public class FunctionStmt extends Stmt {

    public String name = null;
    public boolean isOperator = false;
    public boolean isConstructor = false;
    public String resultType = null;
    public List<Param> parameters = new Vector<>();
    public boolean isVararg = false;
    public String superType = null;
    public BodyStmt implementation = null;
}
