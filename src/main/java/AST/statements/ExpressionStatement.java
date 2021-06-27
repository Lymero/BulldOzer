package AST.statements;

import AST.expressions.Expression;

import java.util.Objects;

public class ExpressionStatement extends Statement
{
    public final Expression expression;

    public ExpressionStatement (Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "ExpressionStatement{" +
                "expression=" + expression +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionStatement that = (ExpressionStatement) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }
}
