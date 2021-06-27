package AST.expressions;

import java.util.List;
import java.util.Objects;

public class FunctionCall extends Expression {

    public Reference reference;
    public List<FunctionParamCall> params;

    public FunctionCall(Reference reference, List<FunctionParamCall> params) {
        this.reference = reference;
        this.params = params;
    }

    @Override
    public String toString() {
        return "FunctionCall{" +
                "varIdentifier=" + reference +
                ", params=" + params +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionCall that = (FunctionCall) o;
        return Objects.equals(reference, that.reference) &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, params);
    }
}
