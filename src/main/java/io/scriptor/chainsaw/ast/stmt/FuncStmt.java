package io.scriptor.chainsaw.ast.stmt;

import java.util.List;
import java.util.Vector;

public class FuncStmt extends Stmt {

    public String ident = null;
    public boolean constructor = false;
    public String type = null;
    public List<Param> params = new Vector<>();
    public boolean vararg = false;
    public String memberOf = null;
    public BodyStmt impl = null;
}
