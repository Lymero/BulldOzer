package AST.types;

import java.util.Objects;

public class CompoundMap extends AbstractType {
    public AbstractType keyType;
    public AbstractType valueType;

    public CompoundMap(AbstractType keyType, AbstractType valueType) {
        super(String.format("map<%s, %s>", keyType.type, valueType.type));
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public String toString() {
        return "CompoundMapType{" +
                "keyType=" + keyType +
                ", valueType=" + valueType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompoundMap that = (CompoundMap) o;
        return Objects.equals(keyType, that.keyType) &&
                Objects.equals(valueType, that.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), keyType, valueType);
    }
}
