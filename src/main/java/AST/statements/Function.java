package AST.statements;

import AST.types.AbstractType;

import java.util.List;
import java.util.Objects;

public class Function extends Declaration {
    public AbstractType returnType;
    public String name;
    public List<Parameter> params;
    public Statement block;

    public Function(AbstractType returnType, String name, List<Parameter> params, Statement block) {
        this.returnType = returnType;
        this.name = name;
        this.params = params;
        this.block = block;
    }

    @Override
    public String toString() {
        return "FunctionDeclaration{" +
                "returnType=" + returnType +
                ", name='" + name + '\'' +
                ", params=" + params +
                ", block=" + block +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Function that = (Function) o;
        return Objects.equals(returnType, that.returnType) &&
                Objects.equals(name, that.name) &&
                Objects.equals(params, that.params) &&
                Objects.equals(block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, name, params, block);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String declaredThing() {
        return "func";
    }
}
