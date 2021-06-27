package types;

public final class VarType extends Type {
    public static final VarType INSTANCE = new VarType();

    private VarType() {
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public String name() {
        return "var";
    }
}