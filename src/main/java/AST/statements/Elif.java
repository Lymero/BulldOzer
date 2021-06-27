package AST.statements;

import AST.expressions.Expression;

import java.util.Objects;

public class Elif extends Statement {
    public Expression condition;
    public Statement statement;

    public Elif(Expression condition, Statement statement) {
        this.condition = condition;
        this.statement = statement;
    }

    @Override
    public String toString() {
        return "ElifStatement{" +
                "condition=" + condition +
                ", statement=" + statement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Elif that = (Elif) o;
        return Objects.equals(condition, that.condition) &&
                Objects.equals(statement, that.statement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, statement);
    }
}
