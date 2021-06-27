package AST.literals;

import AST.expressions.Expression;

import java.util.List;
import java.util.Objects;

public class StringLiteral extends Literal {
    public List<Expression> chunks;

    public StringLiteral(List<Expression> chunks) {
        super(chunks);
        this.chunks = chunks;
    }

    @Override
    public String type() {
        return "Integer";
    }

    @Override
    public String toString() {
        return "StringLiteral{" +
                "literal=" + chunks +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StringLiteral that = (StringLiteral) o;
        return Objects.equals(chunks, that.chunks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), chunks);
    }
}
