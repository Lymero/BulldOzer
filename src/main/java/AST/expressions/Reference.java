package AST.expressions;

import java.util.Objects;

public class Reference extends Expression {
    public String identifier;

    public Reference(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "Reference{" +
                "identifier='" + identifier + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reference reference = (Reference) o;
        return Objects.equals(identifier, reference.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
