package AST.expressions;

import java.util.Objects;

public class Arithmetic extends Expression {
    public Expression left;
    public ArithmeticOperator operator;
    public Expression right;

    public Arithmetic(Expression left, ArithmeticOperator operator, Expression right) {
        super();
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "ArithmeticExpression{" +
                "left=" + left +
                ", operator=" + operator +
                ", right=" + right +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arithmetic that = (Arithmetic) o;
        return Objects.equals(left, that.left) &&
                operator == that.operator &&
                Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, operator, right);
    }
}
