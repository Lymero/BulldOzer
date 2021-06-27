package types;

import java.util.Arrays;
import java.util.Objects;

public final class ArrayType extends Type {
    public static final ArrayType INSTANCE = new ArrayType();
    public Type[] types;
    public Type type;

    private ArrayType() {
    }

    public ArrayType(Type[] types, Type type) {
        this.types = types;
        this.type = type;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public String name() {
        return String.format("array<%s>", type.name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        return Objects.equals(type, arrayType.type);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type);
        result = 31 * result + Arrays.hashCode(types);
        return result;
    }
}
