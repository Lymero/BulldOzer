package AST.expressions;

import java.util.Objects;

public class DataAccess extends Expression {
    public Expression data;
    public Expression key;

    public DataAccess(Expression data, Expression key) {
        this.data = data;
        this.key = key;
    }

    @Override
    public String toString() {
        return "DataAccess{" +
                "data=" + data +
                ", key=" + key +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataAccess that = (DataAccess) o;
        return Objects.equals(data, that.data) &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, key);
    }
}
