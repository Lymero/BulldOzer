package AST;

import AST.statements.Statement;

import java.util.List;
import java.util.Objects;

public class Block extends Statement {
    public List<Statement> statements;

    public Block(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "Block{" +
                "statements=" + statements +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return Objects.equals(statements, block.statements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statements);
    }
}
