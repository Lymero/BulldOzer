package types;

public abstract class Type {

    public abstract String name();

    public boolean isPrimitive() {
        return false;
    }

    public boolean isReference() {
        return !isPrimitive();
    }

    @Override
    public String toString() {
        return name();
    }
}
