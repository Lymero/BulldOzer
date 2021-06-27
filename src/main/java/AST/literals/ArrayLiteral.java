package AST.literals;

import AST.expressions.Expression;

import java.util.List;
import java.util.Objects;

public class ArrayLiteral extends Expression {
    public List<Expression> expressions;

    public ArrayLiteral(List<Expression> list) {
        this.expressions = list;
    }

    @Override
    public String toString() {
        return "Array{" +
                "expressions=" + expressions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayLiteral arrayLiteral = (ArrayLiteral) o;
        return Objects.equals(expressions, arrayLiteral.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }
}
