package io.scriptor.chainsaw.ast.stmt;

import java.util.List;
import java.util.Vector;

public class FuncStmt extends Stmt {

    public String ident;
    public boolean constructor = false;
    public String type;
    public List<Param> params = new Vector<>();
    public boolean vararg = false;
    public BodyStmt impl;
}
