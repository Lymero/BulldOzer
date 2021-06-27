package types;

import java.util.Objects;

public final class TupleType extends Type {
    public static final TupleType INSTANCE = new TupleType();
    public Type type;

    private TupleType() {
    }

    public TupleType(Type type) {
        this.type = type;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public String name() {
        return String.format("tuple<%s>", type.name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TupleType tupleType = (TupleType) o;
        return Objects.equals(type, tupleType.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
