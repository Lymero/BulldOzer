package AST.literals;

import AST.BulldOzerNode;
import AST.expressions.Expression;

import java.util.Objects;

public class PairLiteral extends BulldOzerNode {
    public Expression key;
    public Expression value;

    public PairLiteral(Expression key, Expression value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairLiteral pairLiteral = (PairLiteral) o;
        return Objects.equals(key, pairLiteral.key) &&
                Objects.equals(value, pairLiteral.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
