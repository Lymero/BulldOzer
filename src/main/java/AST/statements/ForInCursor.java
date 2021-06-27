package AST.statements;

import AST.expressions.Expression;

import java.util.Objects;

public class ForInCursor extends Declaration {
    public String name;
    public Expression array;

    public ForInCursor(String name, Expression array) {
        this.name = name;
        this.array = array;
    }

    @Override
    public String toString() {
        return "ForInCursor{" +
                "name='" + name + '\'' +
                ", array=" + array +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForInCursor that = (ForInCursor) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(array, that.array);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, array);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String declaredThing() {
        return "cursor";
    }
}
