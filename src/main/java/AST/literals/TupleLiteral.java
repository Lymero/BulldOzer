package AST.literals;

import AST.expressions.Expression;

import java.util.List;
import java.util.Objects;

public class TupleLiteral extends Expression {
    public List<Expression> elements;

    public TupleLiteral(List<Expression> elements) {
        this.elements = elements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TupleLiteral tupleLiteral = (TupleLiteral) o;
        return Objects.equals(elements, tupleLiteral.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "elements=" + elements +
                '}';
    }
}
