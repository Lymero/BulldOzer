package types;

import java.util.Objects;

public class InCallType extends Type {
    public String name;
    public Type type;

    public InCallType(String name, Type valueTypes) {
        this.name = name;
        this.type = valueTypes;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public String name() {
        return String.format("parameter<%s, %s>", name, type.name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InCallType inCallType = (InCallType) o;
        return Objects.equals(name, inCallType.name) &&
                Objects.equals(type, inCallType.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
