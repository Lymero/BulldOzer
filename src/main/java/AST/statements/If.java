package AST.statements;

import AST.expressions.Expression;

import java.util.List;
import java.util.Objects;

public class If extends Statement {
    public Expression cond;
    public Statement ifStatement;
    public List<Elif> elif;
    public Statement elseStatement;

    public If(Expression cond, Statement ifStatement, List<Elif> elif, Statement elseStatement) {
        this.cond = cond;
        this.ifStatement = ifStatement;
        this.elif = elif;
        this.elseStatement = elseStatement;
    }

    @Override
    public String toString() {
        return "IfStatement{" +
                "cond=" + cond +
                ", ifStatement=" + ifStatement +
                ", elifStatement=" + elif +
                ", elseStatement=" + elseStatement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        If that = (If) o;
        return Objects.equals(cond, that.cond) &&
                Objects.equals(ifStatement, that.ifStatement) &&
                Objects.equals(elif, that.elif) &&
                Objects.equals(elseStatement, that.elseStatement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cond, ifStatement, elif, elseStatement);
    }
}
