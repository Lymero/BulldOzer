package AST.statements;

import AST.expressions.Expression;

import java.util.Objects;

public class While extends Statement {
    public Expression cond;
    public Statement body;

    public While(Expression cond, Statement body) {
        this.cond = cond;
        this.body = body;
    }

    @Override
    public String toString() {
        return "WhileStatement{" +
                "cond=" + cond +
                ", body=" + body +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        While that = (While) o;
        return Objects.equals(cond, that.cond) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cond, body);
    }
}
