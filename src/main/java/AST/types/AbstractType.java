package AST.types;

import AST.BulldOzerNode;

import java.util.Objects;

public abstract class AbstractType extends BulldOzerNode {
    public String type;

    public AbstractType(String type) {
        super();
        this.type = type;
    }

    @Override
    public String toString() {
        return "AbstractType{" +
                "type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractType that = (AbstractType) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
