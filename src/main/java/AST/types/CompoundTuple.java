package AST.types;

import java.util.Objects;

public class CompoundTuple extends AbstractType {
    public AbstractType elementType;

    public CompoundTuple(AbstractType elementType) {
        super("tuple<" + elementType.type + ">");
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        return "CompoundTuple{" +
                "elementType=" + elementType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompoundTuple that = (CompoundTuple) o;
        return Objects.equals(elementType, that.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elementType);
    }
}
