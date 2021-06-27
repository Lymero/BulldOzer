package AST.types;

import java.util.Objects;

public class CompoundArray extends AbstractType {
    public AbstractType elementType;

    public CompoundArray(AbstractType elementType) {
        super("array<" + elementType.type + ">");
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        return "ArrayType{" +
                "elementType=" + elementType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompoundArray arrayType = (CompoundArray) o;
        return Objects.equals(elementType, arrayType.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType);
    }
}
