package AST.literals;

public class ArgcLiteral extends Literal {
    public ArgcLiteral(Object literal) {
        super(literal);
    }

    @Override
    public String type() {
        return "Argc";
    }

    @Override
    public String toString() {
        return "Argc{" + super.toString() + "}";
    }
}
