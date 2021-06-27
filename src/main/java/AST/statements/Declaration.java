package AST.statements;

public abstract class Declaration extends Statement {
    public Declaration() {

    }

    public abstract String name();

    public abstract String declaredThing();
}
