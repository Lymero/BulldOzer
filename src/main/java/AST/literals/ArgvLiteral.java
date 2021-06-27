package AST.literals;

import AST.expressions.Expression;

import java.util.Objects;

public class ArgvLiteral extends Literal {
    public Expression index;
    public ArgvLiteral(Expression index) {
        super(index);
        this.index = index;
    }

    @Override
    public String type() {
        return "Argv";
    }

    @Override
    public String toString() {
        return "Argv{" + super.toString() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ArgvLiteral argvLiteral = (ArgvLiteral) o;
        return Objects.equals(index, argvLiteral.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), index);
    }
}
