package types;

import java.util.Objects;

public final class PairType extends Type {
    public Type keyTypes;
    public Type valueTypes;

    private PairType() {
    }

    public PairType(Type keyTypes, Type valueTypes) {
        this.keyTypes = keyTypes;
        this.valueTypes = valueTypes;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public String name() {
        return String.format("<%s, %s>", keyTypes.name(), valueTypes.name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairType pairType = (PairType) o;
        return Objects.equals(keyTypes, pairType.keyTypes) &&
                Objects.equals(valueTypes, pairType.valueTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyTypes, valueTypes);
    }
}
