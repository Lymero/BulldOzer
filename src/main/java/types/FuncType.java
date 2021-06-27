package types;

import java.util.Arrays;
import java.util.Objects;

public final class FuncType extends Type {
    public final Type returnType;
    public final InFuncType[] inFuncTypes;

    public FuncType(Type returnType, InFuncType[] inFuncTypes) {
        this.returnType = returnType;
        this.inFuncTypes = inFuncTypes;
    }

    @Override
    public String name() {
        return "func -> " + returnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuncType funcType = (FuncType) o;
        return Objects.equals(returnType, funcType.returnType) &&
                Arrays.equals(inFuncTypes, funcType.inFuncTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(returnType);
        result = 31 * result + Arrays.hashCode(inFuncTypes);
        return result;
    }
}