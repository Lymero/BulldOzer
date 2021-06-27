package types;

import java.util.Objects;

public class InFuncType extends Type {
    public String name;
    public Type type;
    public boolean hasDefault;

    public InFuncType(String name, Type valueTypes, boolean hasDefault) {
        this.name = name;
        this.type = valueTypes;
        this.hasDefault = hasDefault;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public String name() {
        return String.format("parameter<%s, %s, %s>", name, type.name(), hasDefault);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InFuncType that = (InFuncType) o;
        return hasDefault == that.hasDefault &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, hasDefault);
    }
}
