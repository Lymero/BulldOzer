package scopes;

import AST.BulldOzerNode;
import AST.statements.Declaration;

import java.util.HashMap;

// source: https://github.com/norswap/sigh/blob/master/src/norswap/sigh/scopes/Scope.java
// with some modifications for the lookup method
public class Scope {

    // node introducing this scope
    public BulldOzerNode node;

    // scope of the parent
    public Scope parent;

    // holds all declarations made in the scope
    private HashMap<String, Declaration> declarations = new HashMap<>();

    public Scope(BulldOzerNode node, Scope parent) {
        this.node = node;
        this.parent = parent;
    }

    // adds a declaration in the scope
    public void declare(String identifier, Declaration node) {
        declarations.put(identifier, node);
    }

    // recursively looks up the declaration
    public DeclarationScope lookup(String name) {
        Declaration declaration = declarations.get(name);
        if (declaration != null) {
            return new DeclarationScope(this, declaration);
        } else if (parent != null) {
            return parent.lookup(name);
        }
        return null;
    }

    // looks up the declaration in the local scope only
    public Declaration lookupLocal(String name) {
        return declarations.get(name);
    }

    @Override
    public String toString() {
        return "Scope " + declarations.toString();
    }


}
