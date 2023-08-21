package io.scriptor.chainsaw.ast;

import java.util.List;
import java.util.Vector;

public class FuncStmt extends Stmt {

    public String ident;
    public String type;
    public List<Param> params = new Vector<>();
    public boolean vararg = false;
    public BodyStmt impl;
}