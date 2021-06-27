package AST;

import AST.statements.Statement;

import java.util.List;
import java.util.Objects;

public final class Root extends BulldOzerNode {
    public final List<Statement> statements;

    public Root(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "Root{" +
                "statements=" + statements +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Root root = (Root) o;
        return Objects.equals(statements, root.statements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statements);
    }
}
