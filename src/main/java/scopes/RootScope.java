package scopes;

import AST.Root;
import norswap.uranium.Reactor;
import types.*;

import static scopes.DeclarationKind.*;

public final class RootScope extends Scope {

    // types
    public final SyntheticDeclaration varType = declare("var", TYPE);
    public final SyntheticDeclaration boolType = declare("bool", TYPE);
    public final SyntheticDeclaration intType = declare("integer", TYPE);
    public final SyntheticDeclaration stringType = declare("string", TYPE);
    public final SyntheticDeclaration voidType = declare("void", TYPE);

    // build-ins
    public final SyntheticDeclaration print = declare("print", FUNCTION);
    public final SyntheticDeclaration parseToInt = declare("parseToInt", FUNCTION);
    public final SyntheticDeclaration parseToString = declare("parseToString", FUNCTION);
    public final SyntheticDeclaration len = declare("len", FUNCTION);

    // global variables
    public final SyntheticDeclaration unknown = declare("unknown", VARIABLE);

    public RootScope(Root node, Reactor reactor) {
        super(node, null);
        registerTypes(reactor);
        registerBuiltIns(reactor);
        registerGlobalVariables(reactor);
    }

    private void registerTypes(Reactor reactor) {
        reactor.set(varType, "type", VarType.INSTANCE);
        reactor.set(boolType, "type", BoolType.INSTANCE);
        reactor.set(intType, "type", IntType.INSTANCE);
        reactor.set(stringType, "type", StringType.INSTANCE);
        reactor.set(voidType, "type", VoidType.INSTANCE);
    }

    private void registerBuiltIns(Reactor reactor) {
        reactor.set(print, "type", new FuncType(
                VoidType.INSTANCE,
                new InFuncType[]{new InFuncType("value", VarType.INSTANCE, false)}
        ));
        reactor.set(parseToInt, "type", new FuncType(
                IntType.INSTANCE,
                new InFuncType[] {new InFuncType("value", StringType.INSTANCE, false)}
        ));
        reactor.set(parseToString, "type", new FuncType(
                StringType.INSTANCE,
                new InFuncType[] {new InFuncType("value", IntType.INSTANCE, false)}
        ));
        reactor.set(len, "type", new FuncType(
                IntType.INSTANCE,
                new InFuncType[] {new InFuncType("value", VarType.INSTANCE, false)}
        ));
    }

    private void registerGlobalVariables(Reactor reactor) {
        reactor.set(unknown, "type", UnknownType.INSTANCE);
    }

    private SyntheticDeclaration declare(String name, DeclarationKind kind) {
        SyntheticDeclaration syntheticDeclaration = new SyntheticDeclaration(name, kind);
        declare(name, syntheticDeclaration);
        return syntheticDeclaration;
    }
}
