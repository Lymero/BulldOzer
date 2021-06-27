package AST.types;

import java.util.Objects;

public class Basic extends AbstractType {
    public String type;

    public Basic(String type) {
        super(type);
        this.type = type;
    }

    @Override
    public String toString() {
        return "Type{" +
                "type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Basic basic1 = (Basic) o;
        return Objects.equals(type, basic1.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}