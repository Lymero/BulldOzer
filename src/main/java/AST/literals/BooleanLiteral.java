package AST.literals;

import java.util.Objects;

public class BooleanLiteral extends Literal {

    private final boolean value;

    public BooleanLiteral(boolean value) {
        super(value);
        this.value = value;
    }

    @Override
    public String type() {
        return "Integer";
    }

    @Override
    public String toString() {
        return "BooleanLiteral{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BooleanLiteral that = (BooleanLiteral) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
