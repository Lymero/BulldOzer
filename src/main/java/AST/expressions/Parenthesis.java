package AST.expressions;

import java.util.Objects;

public class Parenthesis extends Expression {
    public Expression expression;

    public Parenthesis(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "ParenthesisExpression{" +
                "expression=" + expression +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parenthesis that = (Parenthesis) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }
}
