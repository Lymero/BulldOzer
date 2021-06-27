package AST.statements;

import AST.Block;

import java.util.Objects;

public class ForIn extends Statement {
    public ForInCursor cursor;
    public Block body;

    public ForIn(ForInCursor cursor, Block body) {
        this.cursor = cursor;
        this.body = body;
    }

    @Override
    public String toString() {
        return "ForIn{" +
                "cursor=" + cursor +
                ", body=" + body +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForIn forIn = (ForIn) o;
        return Objects.equals(cursor, forIn.cursor) &&
                Objects.equals(body, forIn.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cursor, body);
    }
}
