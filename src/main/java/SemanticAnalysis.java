import AST.Block;
import AST.BulldOzerNode;
import AST.Root;
import AST.expressions.*;
import AST.literals.*;
import AST.statements.*;
import AST.types.Basic;
import AST.types.CompoundArray;
import AST.types.CompoundMap;
import AST.types.CompoundTuple;
import norswap.uranium.Attribute;
import norswap.uranium.Reactor;
import norswap.uranium.Rule;
import norswap.utils.visitors.ReflectiveFieldWalker;
import norswap.utils.visitors.Walker;
import scopes.DeclarationScope;
import scopes.RootScope;
import scopes.Scope;
import types.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static norswap.utils.visitors.WalkVisitType.POST_VISIT;
import static norswap.utils.visitors.WalkVisitType.PRE_VISIT;

public final class SemanticAnalysis {

    private final Reactor reactor;
    private Scope scope;

    private SemanticAnalysis(Reactor reactor) {
        this.reactor = reactor;
    }

    public static Walker<BulldOzerNode> createWalker(Reactor reactor) {
        ReflectiveFieldWalker<BulldOzerNode> walker;
        walker = new ReflectiveFieldWalker<>(BulldOzerNode.class, PRE_VISIT, POST_VISIT);
        SemanticAnalysis analysis = new SemanticAnalysis(reactor);
        registerVisitors(walker, analysis);
        return walker;
    }

    // ===========================================================================================================
    // Register visitors
    // ===========================================================================================================

    private static void registerVisitors(ReflectiveFieldWalker<BulldOzerNode> walker, SemanticAnalysis analysis) {
        registerTypes(walker, analysis);
        registerLiterals(walker, analysis);
        registerDeclarations(walker, analysis);
        registerExpressions(walker, analysis);
        registerStatements(walker, analysis);
        registerScopes(walker, analysis);
        registerFallback(walker);
    }

    private static void registerTypes(ReflectiveFieldWalker<BulldOzerNode> walker, SemanticAnalysis analysis) {
        walker.register(Basic.class, PRE_VISIT, analysis::basicType);
        walker.register(CompoundArray.class, PRE_VISIT, analysis::arrayType);
        walker.register(CompoundMap.class, PRE_VISIT, analysis::mapType);
        walker.register(CompoundTuple.class, PRE_VISIT, analysis::tupleType);
    }

    private static void registerLiterals(ReflectiveFieldWalker<BulldOzerNode> walker, SemanticAnalysis analysis) {
        walker.register(IntegerLiteral.class, PRE_VISIT, analysis::intLiteral);
        walker.register(BooleanLiteral.class, PRE_VISIT, analysis::booleanLiteral);
        walker.register(StringLiteral.class, PRE_VISIT, analysis::stringLiteral);
        walker.register(StringChunk.class, PRE_VISIT, analysis::stringChunk);
        walker.register(StringInterpolation.class, PRE_VISIT, analysis::stringInterpolation);
        walker.register(ArrayLiteral.class, PRE_VISIT, analysis::arrayLiteral);
        walker.register(MapLiteral.class, PRE_VISIT, analysis::mapLiteral);
        walker.register(PairLiteral.class, PRE_VISIT, analysis::pairLiteral);
        walker.register(ArgvLiteral.class, PRE_VISIT, analysis::argvLiteral);
        walker.register(ArgcLiteral.class, PRE_VISIT, analysis::argcLiteral);
        walker.register(TupleLiteral.class, PRE_VISIT, analysis::tupleLiteral);
    }

    private static void registerDeclarations(ReflectiveFieldWalker<BulldOzerNode> walker, SemanticAnalysis analysis) {
        walker.register(Reference.class, PRE_VISIT, analysis::reference);
        walker.register(VarDeclaration.class, PRE_VISIT, analysis::varDeclaration);
        walker.register(Parameter.class, PRE_VISIT, analysis::parameterDeclaration);
        walker.register(ForInCursor.class, PRE_VISIT, analysis::cursorDeclaration);
    }

    private static void registerExpressions(ReflectiveFieldWalker<BulldOzerNode> walker, SemanticAnalysis analysis) {
        walker.register(Assignment.class, PRE_VISIT, analysis::assignment);
        walker.register(Arithmetic.class, PRE_VISIT, analysis::arithmetic);
        walker.register(Parenthesis.class, PRE_VISIT, analysis::parenthesis);
        walker.register(Prefix.class, PRE_VISIT, analysis::prefix);
        walker.register(FunctionCall.class, PRE_VISIT, analysis::functionCall);
        walker.register(FunctionParamCall.class, PRE_VISIT, analysis::functionParamCall);
        walker.register(DataAccess.class, PRE_VISIT, analysis::dataAccess);
    }

    private static void registerStatements(ReflectiveFieldWalker<BulldOzerNode> walker, SemanticAnalysis analysis) {
        walker.register(ExpressionStatement.class, PRE_VISIT, node -> { return; });
        walker.register(Return.class, PRE_VISIT, analysis::returnStatement);
        walker.register(If.class, PRE_VISIT, analysis::ifStatement);
        walker.register(Elif.class, PRE_VISIT, analysis::elifStatement);
        walker.register(While.class, PRE_VISIT, analysis::whileStatement);
        walker.register(ForIn.class, PRE_VISIT, analysis::forInStatement);
    }

    private static void registerScopes(ReflectiveFieldWalker<BulldOzerNode> walker, SemanticAnalysis analysis) {
        walker.register(Root.class, PRE_VISIT, analysis::root);
        walker.register(Root.class, POST_VISIT, analysis::outerScope);
        walker.register(Block.class, PRE_VISIT, analysis::block);
        walker.register(Block.class, POST_VISIT, analysis::outerScope);
        walker.register(Function.class, PRE_VISIT, analysis::functionDeclaration);
        walker.register(Function.class, POST_VISIT, analysis::outerScope);
    }

    private static void registerFallback(ReflectiveFieldWalker<BulldOzerNode> walker) {
        walker.registerFallback(POST_VISIT, node -> { return; } );
    }

    // ===========================================================================================================
    // Types
    // ===========================================================================================================

    /**
     * Handling of the basic types (integer, string, bool...).
     * All possible types are declared in the root scope by default and match those of the parser.
     * Sets:
     * - "value" to the type of the declaration (which is the type itself).
     */
    private void basicType(Basic node) {
        Scope scope = this.scope;
        reactor.rule()
                .by(rule -> {
                    Declaration declaration = scope.lookup(node.type).declaration;
                    reactor.rule(node, "value")
                            .using(declaration, "type")
                            .by(Rule::copyFirst);
                });
    }

    /**
     * Handling of the array type.
     * Sets
     * - "value" to the an arrayType object holding the type of the declaration.
     */
    private void arrayType(CompoundArray node) {
        reactor.rule(node, "value")
                .using(node.elementType, "value")
                .by(rule -> rule.set(0, new ArrayType(new Type[]{}, rule.get(0))));
    }

    /**
     * Handling of the map type.
     * Sets
     * - "value" to the an mapType object holding the pair of types of the declaration.
     */
    private void mapType(CompoundMap node) {
        reactor.rule(node, "value")
                .using(node.keyType.attr("value"), node.valueType.attr("value"))
                .by(rule -> rule.set(0, new MapType(new PairType(rule.get(0), rule.get(1)))));
    }

    /**
     * Handling of the tuple type.
     * Sets
     * - "value" to the an tupleType object holding the type of the declaration.
     */
    private void tupleType(CompoundTuple node) {
        reactor.rule(node, "value")
                .using(node.elementType, "value")
                .by(rule -> rule.set(0, new TupleType(rule.get(0))));
    }

    // ===========================================================================================================
    // Literals
    // ===========================================================================================================

    private void intLiteral(IntegerLiteral node) {
        reactor.set(node, "final", true);
        reactor.set(node, "type", IntType.INSTANCE);
    }

    private void booleanLiteral(BooleanLiteral node) {
        reactor.set(node, "final", true);
        reactor.set(node, "type", BoolType.INSTANCE);
    }

    private void stringLiteral(StringLiteral node) {
        reactor.set(node, "final", true);
        reactor.set(node, "type", StringType.INSTANCE);
    }

    private void stringChunk(StringChunk node) {
        reactor.set(node, "final", true);
        reactor.set(node, "type", StringType.INSTANCE);
    }

    private void stringInterpolation(StringInterpolation node) {
        reactor.set(node, "final", true);
        reactor.rule(node, "type")
                .using(node.expression, "type")
                .by(Rule::copyFirst);
    }

    /**
     * Handling of the array literal.
     * Sets:
     * - "type" to the type of the elements in the array or to unknown if the array is empty.
     * - "final" to true.
     * Error:
     * - checks that each element in the array have a compatible type.
     */
    private void arrayLiteral(ArrayLiteral node) {
        reactor.set(node, "final", true);
        Attribute[] dependencies = node.expressions.stream().map(expr -> expr.attr("type")).toArray(Attribute[]::new);
        reactor.rule(node, "type")
                .using(dependencies)
                .by(rule -> {
                    Type[] types = new Type[node.expressions.size()];
                    if (types.length == 0) {
                        rule.set(0, new ArrayType(types, UnknownType.INSTANCE));
                    } else {
                        boolean isSameType = true;
                        Type currentType = rule.get(0);
                        for (int i = 0; i < types.length; i++) {
                            types[i] = rule.get(i);
                            if (!(types[i] instanceof ArrayType && ((ArrayType) types[i]).type instanceof UnknownType)
                                    && !currentType.equals(types[i])) {
                                isSameType = false;
                                break;
                            }
                            currentType = rule.get(0);
                        }
                        if (isSameType) {
                            rule.set(0, new ArrayType(types, currentType));
                        } else {
                            rule.error("Cannot have different types in a array", node);
                        }
                    }
                });
    }

    /**
     * Handling of a pair.
     * Sets:
     * - "type" to the type of the pair.
     * - "final" to true.
     */
    private void pairLiteral(PairLiteral node) {
        reactor.set(node, "final", true);
        reactor.rule(node, "type")
                .using(node.key.attr("type"), node.value.attr("type"))
                .by(rule -> rule.set(0, new PairType(rule.get(0), rule.get(1))));
    }

    /**
     * Handling of the map literal.
     * Sets:
     * - "type" to the type of the pairs in the map or to a pair of unknown types if the map is empty.
     * - "final" to true.
     * Error:
     * - checks that each pairs in the array have a compatible type.
     */
    private void mapLiteral(MapLiteral node) {
        reactor.set(node, "final", true);
        Attribute[] dependencies = node.pairLiterals.stream().map(expr -> expr.attr("type")).toArray(Attribute[]::new);
        reactor.rule(node, "type")
                .using(dependencies)
                .by(rule -> {
                    if (node.pairLiterals.size() == 0) {
                        rule.set(0, new MapType(new PairType(UnknownType.INSTANCE, UnknownType.INSTANCE)));
                    } else {
                        boolean isSameType = true;
                        PairType currentType = rule.get(0);
                        for (int i = 0; i < node.pairLiterals.size(); i++) {
                            if (!currentType.equals(rule.get(i))) {
                                isSameType = false;
                                break;
                            }
                            currentType = rule.get(0);
                        }
                        if (isSameType) {
                            rule.set(0, new MapType(currentType));
                        } else {
                            rule.error("Cannot have different types in a map", node);
                        }
                    }
                });
    }

    /**
     * Handling of the argv literal (arguments of the program).
     * Sets:
     * - "type" to IntType.
     * - "final" to true.
     * Error:
     * - Checks that the type of the index is IntType.
     */
    private void argvLiteral(ArgvLiteral node) {
        reactor.set(node, "final", true);
        reactor.rule(node, "type")
                .using(node.index, "type")
                .by(rule -> {
                    if (rule.get(0) instanceof IntType) {
                        rule.set(0, StringType.INSTANCE);
                    } else {
                        rule.error("Can only access '$' with index of type integer", node);
                    }
                });
    }

    private void argcLiteral(ArgcLiteral node) {
        reactor.set(node, "final", true);
        reactor.set(node, "type", IntType.INSTANCE);
    }

    /**
     * Handling of the array literal.
     * Sets:
     * - "type" to the type of the elements in the tuple.
     * - "final" to true.
     * Error:
     * - checks that the tuple is not empty.
     * - checks that each element in the array have a compatible type.
     */
    private void tupleLiteral(TupleLiteral node) {
        reactor.set(node, "final", true);
        Attribute[] dependencies = node.elements.stream().map(expr -> expr.attr("type")).toArray(Attribute[]::new);
        reactor.rule(node, "type")
                .using(dependencies)
                .by(rule -> {
                    Type[] types = new Type[node.elements.size()];
                    if (types.length == 0) {
                        rule.error("Cannot declare empty tuple", node);
                    } else {
                        boolean isSameType = true;
                        Type currentType = rule.get(0);
                        for (int i = 0; i < types.length; i++) {
                            types[i] = rule.get(i);
                            if (!currentType.equals(types[i])) {
                                isSameType = false;
                                break;
                            }
                            currentType = rule.get(0);
                        }
                        if (isSameType) {
                            rule.set(0, new TupleType(currentType));
                        } else {
                            rule.error("Cannot have different types in a tuple", node);
                        }
                    }
                });
    }

    // ===========================================================================================================
    // Declarations
    // ===========================================================================================================

    /**
     * Handling of the references.
     * Sets:
     * - "declaration" to the declaration.
     * - "scope" to the the scope of the declaration scope.
     * - "type" to the type of the declaration.
     * - "final" to false.
     * Errors:
     * - Checks that the symbol can be resolved to a declaration.
     */
    private void reference(Reference node) {
        Scope scope = this.scope;
        reactor.set(node, "final", false);
        DeclarationScope declarationScope = scope.lookup(node.identifier);
        if (declarationScope != null) {
            reactor.set(node, "declaration", declarationScope.declaration);
            reactor.set(node, "scope", declarationScope.scope);
            reactor.rule(node, "type")
                    .using(declarationScope.declaration, "type")
                    .by(Rule::copyFirst);
        } else {
            reactor.rule(node.attr("declaration"), node.attr("scope"))
                    .by(rule -> referenceRule(node, scope, rule));
        }
    }

    private void referenceRule(Reference node, Scope scope, Rule rule) {
        DeclarationScope declarationScope = scope.lookup(node.identifier);
        if (declarationScope == null) {
            rule.errorFor(String.format(
                    "Cannot resolve symbol '%s'",
                    node.identifier
            ), node, node.attr("declaration"), node.attr("scope"), node.attr("type"));
        } else {
            Declaration declaration = declarationScope.declaration;
            rule.set(node, "scope", declarationScope.scope);
            rule.set(node, "declaration", declaration);
            if (declaration instanceof VarDeclaration) {
                rule.errorFor(String.format(
                        "Variable '%s' used before declaration", node.identifier
                ), node, node.attr("type"));
            } else {
                reactor.rule(node, "type")
                        .using(declaration, "type")
                        .by(Rule::copyFirst);
            }
        }
    }

    /**
     * Handling of the declaration of a variable.
     * Returns immediately if the value of the variable is the variable itself (e.g. bool x = x).
     * Adds the variable to the scope.
     * Sets:
     * - "scope" to the new scope.
     * - "type" to the value of the expression (not the type), by default the type is inferred even when the
     * "var" type is used.
     * Error:
     * - checks that a variable is not declared with the type "void".
     * - checks that the expression matches the expected type.
     */
    private void varDeclaration(VarDeclaration node) {
        if (node.value instanceof Reference && node.name.equals(((Reference) node.value).identifier)) {
            return;
        }
        scope.declare(node.name, node);
        reactor.set(node, "scope", scope);
        reactor.rule(node, "type")
                .using(node.value.attr("type"), node.type.attr("value"))
                .by(rule -> {
                    Type expressionType = rule.get(0);
                    Type type = rule.get(1);
                    if (expressionType instanceof ArrayType
                            && !(type instanceof VarType)
                            && ((ArrayType) expressionType).type instanceof UnknownType) {
                        rule.set(0, type);
                    } else if (expressionType instanceof MapType
                            && !(type instanceof VarType)
                            && ((MapType) expressionType).keyValueType.keyTypes instanceof UnknownType
                            && ((MapType) expressionType).keyValueType.valueTypes instanceof UnknownType) {
                        rule.set(0, type);
                    } else { // infers the type by default
                        rule.copyFirst();
                    }
                });

        reactor.rule()
                .using(node.type.attr("value"), node.value.attr("type"))
                .by(rule -> checkDeclarationType(node, rule));
    }

    private void checkDeclarationType(VarDeclaration node, Rule rule) {
        Type expected = rule.get(0);
        Type actual = rule.get(1);
        if (node.type.type.equals("void")) {
            rule.error("Cannot declare a variable of type void", node);
        } else if (!isAssignableTo(expected, actual))
            rule.error(String.format(
                    "Required type '%s' but got '%s' for variable '%s'",
                    expected, actual, node.name
            ), node.value);
    }

    /**
     * Handling of a parameter declared in the header of a function.
     * Adds the parameter to the scope.
     * Sets:
     * - "type" to the type of the parameter.
     * - "global-type" to an instance of ParamType.
     * Error:
     * - Checks that the types of the default parameters match the expected type.
     */
    private void parameterDeclaration(Parameter node) {
        scope.declare(node.name, node);
        reactor.rule(node, "type")
                .using(node.type.attr("value"))
                .by(Rule::copyFirst);

        if (node.defaultValue != null) {
            reactor.rule(node, "global-type")
                    .using(node.type.attr("value"), node.defaultValue.attr("type"))
                    .by(rule -> {
                        Type expectedType = rule.get(0);
                        Type defaultValueType = rule.get(1);
                        if (!expectedType.equals(defaultValueType)) {
                            rule.error(String.format(
                                    "Default parameter type '%s' does not match the expected type '%s'",
                                    defaultValueType.name(), expectedType.name()
                            ), node);
                        } else {
                            rule.set(0, new InFuncType(node.name, rule.get(0), node.defaultValue != null));
                        }
                    });
        } else {
            reactor.rule(node, "global-type")
                    .using(node.type.attr("value"))
                    .by(rule -> rule.set(0, new InFuncType(node.name, rule.get(0), node.defaultValue != null)));
        }
    }

    /**
     * Handling of the for-in cursor.
     * Adds the name of the cursor in the scope.
     * Sets:
     * - "type" to the type of the elements in the array.
     * Error:
     * - Checks that the given data type is an array.
     */
    private void cursorDeclaration(ForInCursor node) {
        scope.declare(node.name, node);
        reactor.rule(node, "type")
                .using(node.array, "type")
                .by(rule -> {
                    Type type = rule.get(0);
                    if (type instanceof ArrayType) {
                        ArrayType arrayType = (ArrayType) type;
                        rule.set(0, arrayType.type);
                    } else {
                        rule.error(String.format(
                                "Unexpected type '%s' found in for-in statement",
                                type.name()
                        ), node);
                    }
                });
    }

    // ===========================================================================================================
    // Expressions
    // ===========================================================================================================

    /**
     * Handling of the assignment.
     * Sets:
     * - "type" to the type of the right-side expression unless it is a unknown array.
     * Error:
     * - checks that the left-side entity can be assigned to.
     * - checks that the type of the right-side expression can be assigned to the left-side type.
     * Everything can be assigned to the type "var" and nothing to type "void".
     * The actual type is the right-side type to enable the use of type inference.
     */
    private void assignment(Assignment node) {
        reactor.rule(node.attr("type"))
                .using(node.left.attr("type"), node.right.attr("type"), node.left.attr("final"))
                .by(rule -> {
                    Type left = rule.get(0);
                    Type right = rule.get(1);
                    boolean isFinal = rule.get(2);
                    rule.set(0, right);
                    if (!isFinal) {
                        if (!isAssignableTo(left, right)) {
                            rule.error(String.format(
                                    "Cannot assign type '%s' to type '%s'",
                                    right.name(), left.name()
                            ), node);
                        } else if (left instanceof ArrayType && right instanceof ArrayType) {
                            if (((ArrayType) right).type instanceof UnknownType) {
                                rule.set(0, left);
                            }
                        }
                    } else {
                        rule.errorFor("Final left-side value", node.left);
                    }
                });
    }

    // checks whether an assignment is possible
    private boolean isAssignableTo(Type left, Type right) {
        if (right instanceof VoidType) {
            return false;
        } else if (left instanceof VarType) {
            return true;
        } else if (left instanceof ArrayType && right instanceof ArrayType) {
            return left.equals(right)
                    || ((ArrayType) left).type instanceof UnknownType
                    || ((ArrayType) right).type instanceof UnknownType;
        } else if (left instanceof MapType && right instanceof MapType) {
            return left.equals(right)
                    || ((MapType) left).keyValueType.keyTypes instanceof UnknownType
                    || ((MapType) right).keyValueType.keyTypes instanceof UnknownType;
        }
        return left.equals(right);
    }

    /**
     * Handling of the arithmetic expression.
     * Sets:
     * - "type" to the type of the expression.
     * - "final" to true.
     * (string for concatenations, array for array appends, map for array appends, integer for arithmetic expressions,
     * boolean for comparisons and boolean expressions)
     * Error:
     * - checks that a numeric expression has only integer values as operands;
     * - checks that a numeric comparison has only integer values as operands.
     * - checks that boolean logic is performed with only bool values as operands.
     * - checks that a equality is performed with the operands of the same type.
     */
    private void arithmetic(Arithmetic node) {
        reactor.set(node, "final", true);
        reactor.rule(node, "type")
                .using(node.left.attr("type"), node.right.attr("type"))
                .by(rule -> setArithmeticRule(node, rule, rule.get(0), rule.get(1)));
    }

    private void setArithmeticRule(Arithmetic node, Rule rule, Type left, Type right) {
        if (isConcatenation(node.operator, left, right)) {
            rule.set(0, StringType.INSTANCE);
        } else if (isArrayArithmetic(node.operator, left, right)) { // TODO this is a bonus feature!
            arrayArithmetic(rule, node, left, right);
        } else if (isMapArithmetic(node.operator, left, right)) { // TODO this is a bonus feature!
            rule.set(0, MapType.INSTANCE);
        } else if (isArithmetic(node.operator)) {
            arithmetic(rule, node, left, right);
        } else if (isNumericComparison(node.operator)) {
            numericComparison(rule, node, left, right);
        } else if (isLogic(node.operator)) {
            booleanLogic(rule, node, left, right);
        } else if (isEquality(node.operator)) {
            equality(rule, node, left, right);
        }
    }

    private boolean isConcatenation(ArithmeticOperator operator, Type left, Type right) {
        return operator == ArithmeticOperator.PLUS
                && left instanceof StringType
                && right instanceof StringType;
    }

    private boolean isArrayArithmetic(ArithmeticOperator operator, Type left, Type right) {
        return operator == ArithmeticOperator.PLUS
                && left instanceof ArrayType
                && right instanceof ArrayType;
    }

    private void arrayArithmetic(Rule rule, Arithmetic node, Type left, Type right) {
        ArrayType typeLeft = (ArrayType) left;
        ArrayType typeRight = (ArrayType) right;
        if (typeLeft.type.equals(typeRight.type)) {
            int numberTypesLeft = typeLeft.types.length;
            int numberTypesRight = typeRight.types.length;
            Type[] concatenate = new Type[numberTypesLeft + numberTypesRight];
            System.arraycopy(typeLeft.types, 0, concatenate, 0, numberTypesLeft);
            System.arraycopy(typeRight.types, 0, concatenate, numberTypesLeft, numberTypesRight);
            rule.set(0, new ArrayType(concatenate, typeLeft.type));
        } else if (typeRight.type instanceof UnknownType) {
            rule.set(0, new ArrayType(typeLeft.types, typeLeft.type));
        } else if (typeLeft.type instanceof UnknownType) {
            rule.set(0, new ArrayType(typeRight.types, typeRight.type));
        } else {
            rule.error(String.format("Cannot append type '%s' to type '%s'", typeLeft, typeRight), node);
        }
    }

    private boolean isMapArithmetic(ArithmeticOperator operator, Type left, Type right) {
        return operator == ArithmeticOperator.PLUS
                && left instanceof MapType
                && right instanceof MapType;
    }

    private boolean isArithmetic(ArithmeticOperator operator) {
        return operator == ArithmeticOperator.PLUS
                || operator == ArithmeticOperator.MINUS
                || operator == ArithmeticOperator.TIMES
                || operator == ArithmeticOperator.DIV
                || operator == ArithmeticOperator.MOD;
    }

    // sets the type and checks that a numeric expression has only integer values as operands
    private void arithmetic(Rule rule, Arithmetic node, Type left, Type right) {
        if (left instanceof IntType && right instanceof IntType) {
            rule.set(0, IntType.INSTANCE);
        } else {
            rule.error(String.format(
                    "Invalid arithmetic expression: %s %s %s",
                    left, node.operator.name().toLowerCase(), right
            ), node);
        }
    }

    private boolean isNumericComparison(ArithmeticOperator operator) {
        return operator == ArithmeticOperator.GT
                || operator == ArithmeticOperator.LT
                || operator == ArithmeticOperator.GTEQ
                || operator == ArithmeticOperator.LTEQ;
    }

    // sets the type and checks that a numeric comparison has only integer values as operands
    public void numericComparison(Rule rule, Arithmetic node, Type left, Type right) {
        rule.set(0, BoolType.INSTANCE);
        if (!(left instanceof IntType) || !(right instanceof IntType)) {
            rule.errorFor(String.format(
                    "Comparison on non-numeric type: %s %s %s",
                    left, node.operator.name(), right
            ), node.left);
        }
    }

    private boolean isLogic(ArithmeticOperator operator) {
        return operator == ArithmeticOperator.OR
                || operator == ArithmeticOperator.AND;
    }

    // sets the type and checks that boolean logic is performed with only bool values as operands
    private void booleanLogic(Rule rule, Arithmetic node, Type left, Type right) {
        rule.set(0, BoolType.INSTANCE);
        if (!(left instanceof BoolType) || !(right instanceof BoolType)) {
            rule.errorFor(String.format(
                    "Boolean logic on non-boolean type: %s %s %s ",
                    left, node.operator.name(), node.left
            ), node);
        }
    }

    private boolean isEquality(ArithmeticOperator operator) {
        return operator == ArithmeticOperator.EQEQ
                || operator == ArithmeticOperator.BANGEQ;
    }

    // sets the type and checks that a equality is performed with the operands of the same type
    private void equality(Rule rule, Arithmetic node, Type left, Type right) {
        rule.set(0, BoolType.INSTANCE);
        if (!isAssignableTo(left, right)
                && !(left instanceof UnknownType)
                && !(right instanceof UnknownType)) { // same-type values can be compared only
            rule.errorFor(String.format(
                    "Equality on incomparable types: %s %s",
                    left, right
            ), node);
        }
    }

    /**
     * Handling of the prefix expression.
     * Sets:
     * - "type" to the type of the expression.
     * - "final" to true.
     */
    private void parenthesis(Parenthesis node) {
        reactor.set(node, "final", true);
        reactor.rule(node, "type")
                .using(node.expression, "type")
                .by(Rule::copyFirst);
    }

    /**
     * Handling of the prefix expression.
     * For now, only the ! prefix is allowed.
     * Sets:
     * - "type" to the bool type.
     * - "final" to true.
     * Error:
     * - checks that it is a bool expression.
     */
    private void prefix(Prefix node) {
        reactor.set(node, "final", true);
        reactor.set(node, "type", BoolType.INSTANCE);
        reactor.rule()
                .using(node.postfixExpression, "type")
                .by(rule -> checkPrefixExpressionTypeRule(node, rule));
    }

    // checks that it is a bool expression
    private void checkPrefixExpressionTypeRule(Prefix node, Rule rule) {
        Type operationType = rule.get(0);
        if (!(operationType.getClass().equals(BoolType.class))) {
            rule.error(String.format(
                    "Cannot apply operator '%s' on type '%s'",
                    node.prefix, operationType
            ), node);
        }
    }

    /**
     * Handling of the call of a function.
     * Sets:
     * - "type" to return-type of the declared function.
     * - "index" (for each parameter) to the index of each parameter.
     * Error:
     * - checks that the callee is a function.
     * - checks that the number of given arguments match the number of required parameters while taking into account
     *   the default parameters.
     * - checks that the types of the given arguments match the types of the required parameters.
     * - checks that either only named parameters or no named-parameters are used.
     * - checks that there are no named parameters duplicate.
     * - checks that named parameters can be matched with the parameters of the function called.
     */
    private void functionCall(FunctionCall node) {
        reactor.rule(node, "type")
                .using(getFunctionDependencies(node))
                .by(rule -> {
                    Type referenceType = rule.get(0);
                    if (!(referenceType instanceof FuncType)) {
                        rule.error(String.format(
                                "Function call expected: cannot call '%s' of type '%s'",
                                node.reference.identifier, referenceType
                        ), node.reference);
                    } else {
                        FuncType funType = (FuncType) referenceType;
                        rule.set(0, funType.returnType);
                        checkParameters(node, rule, funType);
                    }
                });
    }

    // retrieves the type of the function and the types of its parameters.
    // sets the index of each parameter.
    private Attribute[] getFunctionDependencies(FunctionCall node) {
        List<FunctionParamCall> parameters = node.params;
        Attribute[] dependencies = new Attribute[parameters.size() + 1];
        dependencies[0] = node.reference.attr("type");
        for (int i = 0; i < parameters.size(); i++) {
            dependencies[i + 1] = parameters.get(i).attr("global-type");
            reactor.set(parameters.get(i), "index", i);
            reactor.set(parameters.get(i), "name", parameters.get(i).namedParameter);
        }
        return dependencies;
    }

    // checks that the types of the given arguments match the types of the required parameters.
    private void checkParameters(FunctionCall node, Rule rule, FuncType funType) {
        // retrieves all named parameters in the function call
        Set<InCallType> namedParameters = new HashSet<>();
        for (int i = 0; i < node.params.size(); i++) {
            InCallType namedParameter = rule.get(i + 1);
            String parameterName = namedParameter.name;
            if (parameterName != null) {
                if (namedParameters.contains(namedParameter)) {
                    rule.error(String.format("Duplicate parameter name '%s'", parameterName), node);
                    return;
                }
                namedParameters.add(namedParameter);
            }
        }

        long nDefaultParams = Arrays.stream(funType.inFuncTypes).filter(inFuncType -> inFuncType.hasDefault).count();
        // all parameters are set default
        if (nDefaultParams == funType.inFuncTypes.length && node.params.size() == 0) return;
        // more params than expected
        if (node.params.size() > funType.inFuncTypes.length) {
            rule.error("More parameters than expected were given", node);
            return;
        }

        if (namedParameters.size() == 0) { // non-named parameters only
            checkNonNamedParameters(node, rule, funType, nDefaultParams);
        } else if (namedParameters.size() ==  node.params.size()) { // named parameters only
            checkNamedParameters(node, rule, funType);
        } else  { // mix of named and non-named parameters
            rule.error("Either all parameters are named or none", node);
        }
    }

    private void checkNonNamedParameters(FunctionCall node, Rule rule, FuncType funType, long nDefaultParams) {
        // less params than expected
        if (node.params.size() + nDefaultParams < funType.inFuncTypes.length) {
            rule.error("Less parameters than expected were given", node);
            return;
        }
        // not empty call and less params than expected
        if (nDefaultParams > 0 && node.params.size() < funType.inFuncTypes.length) {
            rule.error("Cannot use default parameters with non-named parameters", node);
            return;
        }
        // checks if types match using the position
        for (int i = 0; i < funType.inFuncTypes.length; i++) {
            Type inFuncType = funType.inFuncTypes[i].type;
            Type inCallType = ((InCallType) rule.get(i + 1)).type;
            if (!isAssignableTo(inFuncType, inCallType)) {
                rule.errorFor(String.format(
                        "Expected argument at position '%d' of type '%s' but got '%s'",
                        i, inFuncType.name(), inCallType.name()
                ), node.params.get(i));
            }
        }
    }

    private void checkNamedParameters(FunctionCall node, Rule rule, FuncType funType) {
        for (int i = 0; i < funType.inFuncTypes.length; i++) {
            InFuncType inFuncType = funType.inFuncTypes[i];
            InCallType matchedInCallType = null;
            for (int j = 0; j < node.params.size(); j++) {
                InCallType inCallType = rule.get(j + 1);
                if (inFuncType.name.equals(inCallType.name)) {
                    matchedInCallType = inCallType;
                }
            }
            if (matchedInCallType != null) {
                if (!isAssignableTo(inFuncType.type, matchedInCallType.type)) {
                    rule.errorFor(String.format(
                            "Expected argument at position '%d' of type '%s' but got '%s'",
                            i, inFuncType.type.name(), matchedInCallType.type.name()
                    ), node.params.get(i));
                }
            } else if (!inFuncType.hasDefault) {
                rule.error(String.format("Parameter '%s' cannot be found", inFuncType.name), node);
            }
        }
    }

    /**
     * Handling of the function parameters during a function call.
     * Sets:
     * - "type" to the type of the expression.
     */
    private void functionParamCall(FunctionParamCall node) {
        reactor.rule(node, "type")
                .using(node.value, "type")
                .by(Rule::copyFirst);
        reactor.rule(node, "global-type")
                .using(node.value, "type")
                .by(rule -> rule.set(0, new InCallType(node.namedParameter, rule.get(0))));
    }

    /**
     * Handling of the data access (array/map).
     * Sets:
     * - "type" to the type of the elements of the array or the value-type of the map.
     * - "final" to true is the accessed entity is a tuple, to false otherwise.
     * Error:
     * - checks that the array is accessed via an integer.
     * - checks that the array is accessed via a key of the same type as the expected type of the map.
     */
    private void dataAccess(DataAccess node) {
        reactor.rule()
                .using(node.key.attr("type"), node.data.attr("type"))
                .by(rule -> checkKeyTypeDataAccessRule(node, rule));

        reactor.rule(node, "type")
                .using(node.data.attr("type"))
                .by(this::setDataAccessType);

        reactor.rule(node, "final")
                .using(node.data.attr("type"))
                .by(this::setFinalRule);
    }

    private void checkKeyTypeDataAccessRule(DataAccess node, Rule rule) {
        Type keyType = rule.get(0);
        Type dataType = rule.get(1);
        if ((dataType instanceof ArrayType || dataType instanceof TupleType)
                && !(keyType instanceof IntType)) {
            rule.error(String.format(
                    "Cannot access type '%s' with key of type '%s'",
                    dataType.name(), keyType.name()
            ), node);
        } else if (dataType instanceof MapType) {
            Type mapKey = ((MapType) dataType).keyValueType.keyTypes;
            if (!keyType.equals(mapKey)) {
                rule.error(String.format(
                        "Cannot access map of type '%s' with key of type '%s'",
                        dataType.name(), keyType.name()
                ), node);
            }
        }
    }

    // sets "type" to the type of the elements of the array or the value-type of the map
    private void setDataAccessType(Rule rule) {
        Type dataType = rule.get(0);
        if (dataType instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) dataType;
            rule.set(0, arrayType.type);
        } else if (dataType instanceof MapType) {
            MapType mapType = (MapType) dataType;
            rule.set(0, mapType.keyValueType.valueTypes);
        } else if (dataType instanceof TupleType) {
            TupleType tupleType = (TupleType) dataType;
            rule.set(0, tupleType.type);
        }
    }

    // sets "final" to true is the accessed entity is a tuple, to false otherwise.
    private void setFinalRule(Rule rule) {
        if (rule.get(0) instanceof TupleType) {
            rule.set(0, true);
        } else {
            rule.set(0, false);
        }
    }

    // ===========================================================================================================
    // Scopes
    // ===========================================================================================================

    /**
     * Handling of the root.
     * Sets:
     * - "scope" to a new scope with the function.
     */
    private void root(Root node) {
        scope = new RootScope(node, reactor);
        reactor.set(node, "scope", scope);
    }

    /**
     * Handling of the block statement.
     * Sets:
     * - "scope" to a new scope with the function.
     * - "returns" to true if the block contains a return statement, false otherwise.
     * - "types" to the type of the returning statements (to infer the type of a function for the "var" type).
     * The type is set to "void" if the function does not return anything.
     */
    private void block(Block node) {
        scope = new Scope(node, scope);
        reactor.set(node, "scope", scope);
        Attribute[] returns = getReturnsDependencies(node.statements, "returns");
        reactor.rule(node, "returns")
                .using(returns)
                .by(rule -> rule.set(0, returns.length != 0 && Arrays.stream(returns).anyMatch(rule::get)));

        Attribute[] types = getReturnsDependencies(node.statements, "type");
        reactor.rule(node, "type")
                .using(types)
                .by(rule -> {
                    if (types.length != 0) {
                        rule.copyFirst();
                    } else {
                        rule.set(0, VoidType.INSTANCE);
                    }
                });
    }

    // source: https://github.com/norswap/sigh/blob/08c412108c75f177aad59571df3a188c371749d2/src/norswap/sigh/SemanticAnalysis.java#L907
    // modified to allow the retrieval of an attribute with a parametrized name.
    private Attribute[] getReturnsDependencies(List<? extends BulldOzerNode> children, String name) {
        return children.stream()
                .filter(Objects::nonNull)
                .filter(this::isReturnContainer)
                .map(container -> container.attr(name))
                .toArray(Attribute[]::new);
    }

    private boolean isReturnContainer(BulldOzerNode node) {
        return node instanceof Block
                || node instanceof If
                || node instanceof While
                || node instanceof ForIn
                || node instanceof Return;
    }

    /**
     * Handling of the declaration of a function.
     * Sets:
     * - "scope" to a new scope with the function.
     * - "type" to the actual type returned in the case the return type is "var" (via inference),
     * otherwise sets "type" to the default type of function specified in its header.
     * Errors:
     * - checks that a return statement is found is the function does not return "void".
     */
    private void functionDeclaration(Function node) {
        scope.declare(node.name, node);
        scope = new Scope(node, scope);
        reactor.set(node, "scope", scope);

        Attribute[] dependencies = getFunctionDeclarationDependencies(node);
        reactor.rule(node, "type")
                .using(dependencies)
                .by(rule -> setFunctionTypeRule(node, rule));

        reactor.rule()
                .using(node.block.attr("returns"), node.returnType.attr("value"))
                .by(rule -> checkFunctionReturnTypeRule(node, rule));
    }

    // builds a list of attributes containing the function's return-type, the type of its direct block
    // and the type of its params.
    private Attribute[] getFunctionDeclarationDependencies(Function node) {
        Attribute[] dependencies = new Attribute[node.params.size() + 2];
        dependencies[0] = node.returnType.attr("value");
        dependencies[1] = node.block.attr("type");
        List<Parameter> parameters = node.params;
        for (int i = 0; i < parameters.size(); i++) {
            dependencies[i + 2] = parameters.get(i).attr("global-type");
        }
        return dependencies;
    }

    // either infers the type using the actual returning type or use the function's return-type
    private void setFunctionTypeRule(Function node, Rule rule) {
        InFuncType[] parameterTypes = new InFuncType[node.params.size()];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = rule.get(i + 2);
        }
        if (rule.get(0) instanceof VarType) {
            rule.set(0, new FuncType(rule.get(1), parameterTypes));
        } else {
            rule.set(0, new FuncType(rule.get(0), parameterTypes));
        }
    }

    // checks that a return statement is found is the function does not return "void"
    private void checkFunctionReturnTypeRule(Function node, Rule rule) {
        boolean returns = rule.get(0);
        Type returnType = rule.get(1);
        if (!(returnType instanceof VoidType) && !returns) {
            rule.error("Expected a return statement but found none", node);
        }
    }

    // end of scope, next scope is the scope of the parent
    private void outerScope(BulldOzerNode node) {
        scope = scope.parent;
    }

    // ===========================================================================================================
    // Statements
    // ===========================================================================================================

    /**
     * Rules for the "return" statements.
     * Sets:
     * - "returns" to true: so that the function know that there is a returning statement.
     * - "type" to the type of what is returned: so that the type of the function can be inferred. If there is no
     * returning expression then the type is set to void.
     * Errors:
     * - checks that the return statement is in a function.
     * - checks that the return statement returns a value if the function is not void.
     * - checks that the returning types match the function type.
     */
    private void returnStatement(Return node) { // TODO add return statement with multiple values
        Function function = currentFunction();
        reactor.set(node, "returns", true);
        if (node.expression.size() > 0) {
            reactor.rule(node, "type")
                    .using(node.expression.get(0), "type")
                    .by(Rule::copyFirst);
        } else {
            reactor.rule(node, "type").by(rule -> rule.set(0, VoidType.INSTANCE));
        }
        if (function == null) {
            reactor.rule().by(rule -> rule.error("Cannot return outside of a function", node));
        } else if (node.expression.size() == 0) {
            checkEmptyReturnRule(node, function);
        } else {
            checkIncompatibleTypeReturnRule(node, function);
        }
    }

    // checks that the return statement returns a value if the function is not void
    private void checkEmptyReturnRule(Return node, Function function) {
        reactor.rule()
                .using(function.returnType, "value")
                .by(rule -> {
                    Type returnType = rule.get(0);
                    if (!(returnType instanceof VoidType)) {
                        rule.error("Expected a non-empty return statement", node);
                    }
                });
    }

    // checks that the returning types match the function type
    private void checkIncompatibleTypeReturnRule(Return node, Function function) {
        reactor.rule()
                .using(function.returnType.attr("value"), node.expression.get(0).attr("type"))
                .by(rule -> {
                    Type expected = rule.get(0);
                    Type actual = rule.get(1);
                    if (!isAssignableTo(expected, actual)) {
                        rule.errorFor(String.format(
                                "Incompatible types: required '%s' but got '%s' for function '%s'",
                                expected, actual, function.name
                        ), node.expression);
                    }
                });
    }

    // looks up current function in the current scope
    private Function currentFunction() {
        Scope scope = this.scope;
        while (scope != null) {
            BulldOzerNode node = scope.node;
            if (node instanceof Function) {
                return (Function) node;
            }
            scope = scope.parent;
        }
        return null;
    }

    /**
     * Handling of the if statement.
     * Sets:
     * - "type" to the return type if present, or to Voidtype otherwise.
     * - "returns" to true is there is a return statement, false otherwise.
     * Error:
     * - Checks that the condition is a boolean expression.
     */
    private void ifStatement(If node) {
        reactor.rule()
                .using(node.cond.attr("type"))
                .by(rule -> checkStatementCondition(node, rule));

        List<Statement> statements = Stream
                .of(Arrays.asList(node.ifStatement, node.elseStatement), node.elif)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Attribute[] returns = getReturnsDependencies(statements, "returns");
        reactor.rule(node, "returns")
                .using(returns)
                .by(rule -> rule.set(0, Arrays.stream(returns).allMatch(rule::get)));

        Attribute[] types = getReturnsDependencies(statements, "type");
        reactor.rule(node, "type")
                .using(types)
                .by(rule -> {
                    if (types.length != 0) {
                        rule.copyFirst();
                    } else {
                        rule.set(0, VoidType.INSTANCE);
                    }
                });
    }

    /**
     * Handling of the elif statement.
     * Sets:
     * - "type"  to the type  attribute of its body.
     * - "returns" to the returns attribute of its body.
     * Error:
     * - Checks that the condition is a boolean expression.
     */
    private void elifStatement(Elif node) {
        reactor.rule()
                .using(node.condition.attr("type"))
                .by(rule -> checkStatementCondition(node, rule));

        reactor.rule(node, "returns")
                .using(node.statement, "returns")
                .by(Rule::copyFirst);

        reactor.rule(node, "type")
                .using(node.statement, "type")
                .by(Rule::copyFirst);
    }

    /**
     * Handling of the while statement.
     * Sets:
     * - "type"  to the type  attribute of its body.
     * - "returns" to the returns attribute of its body.
     * Error:
     * - Checks that the condition is a boolean expression.
     */
    private void whileStatement(While node) {
        reactor.rule()
                .using(node.cond, "type")
                .by(rule -> checkStatementCondition(node, rule));

        reactor.rule(node, "returns")
                .using(node.body, "returns")
                .by(Rule::copyFirst);

        reactor.rule(node, "type")
                .using(node.body, "type")
                .by(Rule::copyFirst);
    }

    // checks that the condition is a boolean expression
    private void checkStatementCondition(Statement node, Rule rule) {
        if (!(rule.get(0) instanceof BoolType)) {
            rule.error(String.format(
                    "Expected condition of type 'bool' but got '%s'",
                    (Type) rule.get(0)
            ), node);
        }
    }

    /**
     * Handling of the for-in statement.
     * Sets:
     * - "type" to the type of its body.
     * - "returns" to the "returns" value of its body.
     */
    private void forInStatement(ForIn node) {
        reactor.rule(node, "returns")
                .using(node.body, "returns")
                .by(Rule::copyFirst);

        reactor.rule(node, "type")
                .using(node.body, "type")
                .by(Rule::copyFirst);
    }
}