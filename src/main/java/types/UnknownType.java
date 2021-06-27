package types;

public final class UnknownType extends Type {
    public static final UnknownType INSTANCE = new UnknownType();

    private UnknownType() {
    }

    @Override
    public String name() {
        return "unknown";
    }
}
