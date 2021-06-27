package AST.literals;

import AST.expressions.Expression;

import java.util.Objects;

public class StringChunk extends Expression {
    public String chunk;

    public StringChunk(String chunk) {
        this.chunk = chunk;
    }

    @Override
    public String toString() {
        return "StringChunk{" +
                "chunk='" + chunk + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringChunk that = (StringChunk) o;
        return Objects.equals(chunk, that.chunk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunk);
    }
}
