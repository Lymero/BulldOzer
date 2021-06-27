package types;

import java.util.Objects;

public final class MapType extends Type {
    public static final MapType INSTANCE = new MapType();
    public PairType keyValueType;

    private MapType() {
    }

    public MapType(PairType keyValueType) {
        this.keyValueType = keyValueType;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public String name() {
        return String.format("map%s", keyValueType.name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapType mapType = (MapType) o;
        return Objects.equals(keyValueType, mapType.keyValueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyValueType);
    }
}
