package AST.statements;

import AST.expressions.Expression;
import AST.types.AbstractType;

import java.util.Objects;

public class VarDeclaration extends Declaration {
    public AbstractType type;
    public String name;
    public Expression value;

    public VarDeclaration(AbstractType type, String name, Expression value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "VarDeclaration{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarDeclaration that = (VarDeclaration) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, value);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String declaredThing() {
        return "var";
    }
}
