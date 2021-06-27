package AST.expressions;

import java.util.Objects;

public class FunctionParamCall extends Expression {
    public String namedParameter;
    public Expression value;

    public FunctionParamCall(String reference, Expression value) {
        this.namedParameter = reference;
        this.value = value;
    }

    @Override
    public String toString() {
        return "FunctionParamCall{" +
                "reference=" + namedParameter +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionParamCall that = (FunctionParamCall) o;
        return Objects.equals(namedParameter, that.namedParameter) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namedParameter, value);
    }
}
