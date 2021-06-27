package scopes;

import AST.statements.Declaration;

// source: https://github.com/norswap/sigh/blob/master/src/norswap/sigh/scopes/DeclarationContext.java
public final class DeclarationScope {
    public final Scope scope;
    public final Declaration declaration;

    public DeclarationScope(Scope scope, Declaration declaration) {
        this.scope = scope;
        this.declaration = declaration;
    }
}
