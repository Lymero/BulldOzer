package AST.literals;

import java.util.Objects;

public final class IntegerLiteral extends Literal {
    public final long value;

    public IntegerLiteral(long value) {
        super(value);
        this.value = value;
    }

    @Override
    public String type() {
        return "Integer";
    }

    @Override
    public String toString() {
        return "IntegerLiteral{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IntegerLiteral that = (IntegerLiteral) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
