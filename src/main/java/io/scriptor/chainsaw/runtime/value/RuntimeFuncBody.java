package io.scriptor.chainsaw.runtime.value;

import java.util.Collections;
import java.util.List;

import io.scriptor.chainsaw.ast.stmt.Stmt;

public class RuntimeFuncBody extends FuncBody {

    private List<Stmt> stmts;

    public RuntimeFuncBody(Function function, List<Stmt> stmts) {
        super(function);
        this.stmts = stmts;
    }

    public List<Stmt> getStmts() {
        return Collections.unmodifiableList(stmts);
    }

}
