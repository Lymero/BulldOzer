package AST.statements;

import AST.expressions.Expression;

import java.util.List;
import java.util.Objects;

public class Return extends Statement {
    public List<Expression> expression;

    public Return(List<Expression> expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "ReturnStatement{" +
                "expression=" + expression +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Return that = (Return) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }
}
