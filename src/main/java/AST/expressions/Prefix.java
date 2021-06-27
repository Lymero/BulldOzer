package AST.expressions;

import java.util.Objects;

public class Prefix extends Expression {
    public PrefixOperator prefix;
    public Expression postfixExpression;

    public Prefix(PrefixOperator prefix, Expression postfixExpression) {
        this.prefix = prefix;
        this.postfixExpression = postfixExpression;
    }

    @Override
    public String toString() {
        return "PrefixExpression{" +
                "prefix=" + prefix +
                ", postfixExpression=" + postfixExpression +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prefix that = (Prefix) o;
        return prefix == that.prefix &&
                Objects.equals(postfixExpression, that.postfixExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, postfixExpression);
    }
}
