package scopes;

import AST.statements.Declaration;

import java.util.Locale;

// source: https://github.com/norswap/sigh/blob/master/src/norswap/sigh/scopes/SyntheticDeclarationNode.java
public final class SyntheticDeclaration extends Declaration {
    private final String name;
    private final DeclarationKind kind;

    public SyntheticDeclaration(String name, DeclarationKind kind) {
        this.name = name;
        this.kind = kind;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String declaredThing() {
        return "built-in " + kind.name().toLowerCase(Locale.ROOT);
    }
}
