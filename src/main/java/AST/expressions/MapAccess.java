package AST.expressions;

import java.util.Objects;

public class MapAccess extends Expression {
    public Expression element;
    public Expression key;

    public MapAccess(Expression element, Expression key) {
        this.element = element;
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapAccess mapAccess = (MapAccess) o;
        return Objects.equals(element, mapAccess.element) &&
                Objects.equals(key, mapAccess.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, key);
    }

    @Override
    public String toString() {
        return "MapAccess{" +
                "element=" + element +
                ", key=" + key +
                '}';
    }
}
