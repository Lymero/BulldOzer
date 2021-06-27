package AST.statements;

import AST.expressions.Expression;
import AST.types.AbstractType;

import java.util.Objects;

public class Parameter extends Declaration {
    public AbstractType type;
    public String name;
    public Expression defaultValue;

    public Parameter(AbstractType type, String name, Expression defaultValue) {
        this.type = type;
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "ParameterDeclaration{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", defaultValue=" + defaultValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter that = (Parameter) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, defaultValue);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String declaredThing() {
        return "parameter";
    }
}
