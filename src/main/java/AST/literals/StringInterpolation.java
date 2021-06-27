package AST.literals;

import AST.expressions.Expression;

import java.util.Objects;

public class StringInterpolation extends Expression {
    public Expression expression;

    public StringInterpolation(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "StringInterpolation{" +
                "expression=" + expression +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringInterpolation that = (StringInterpolation) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }
}
