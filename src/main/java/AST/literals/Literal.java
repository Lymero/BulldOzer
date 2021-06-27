package AST.literals;

import AST.expressions.Expression;

import java.util.Objects;

public abstract class Literal extends Expression {
    public Object literal;

    public Literal(Object literal) {
        this.literal = literal;
    }

    public abstract String type();

    @Override
    public String toString() {
        return "Literal{" +
                "literal=" + literal +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Literal literal1 = (Literal) o;
        return Objects.equals(literal, literal1.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(literal);
    }
}