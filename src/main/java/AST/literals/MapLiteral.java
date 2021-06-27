package AST.literals;

import AST.expressions.Expression;

import java.util.List;
import java.util.Objects;

public class MapLiteral extends Expression {
    public List<PairLiteral> pairLiterals;

    public MapLiteral(List<PairLiteral> pairLiterals) {
        this.pairLiterals = pairLiterals;
    }

    @Override
    public String toString() {
        return "Map{" +
                "pairs=" + pairLiterals +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapLiteral mapLiteral = (MapLiteral) o;
        return Objects.equals(pairLiterals, mapLiteral.pairLiterals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pairLiterals);
    }
}
