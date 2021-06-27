import AST.expressions.*;
import AST.literals.*;
import AST.types.Basic;
import norswap.autumn.AutumnTestFixture;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class ParserExpressionsTests extends AutumnTestFixture {

    GrammarParser parser = new GrammarParser();

    @Test public void testType() {
        this.rule = parser.type;
        successExpect("integer", new Basic("integer"));
        success("var");
        success("integer");
        success("bool");
        success("string");
        success("array<integer>");
        success("array<bool>");
        success("array<array<integer>>");

        failure("array");
        failure("array<");
        failure("array<>");
        failure("array<5>");
        failure("str");
    }

    @Test
    public void testCompareOperators() {
        this.rule = parser.comparison_op;
        successExpect("==", ArithmeticOperator.EQEQ);
        success(">");
        success("<");
        success("!=");
        success(">=");
        success("<=");

        failure("=");
    }

    @Test
    public void testParenthesis() {
        this.rule = parser.par_expr;
        successExpect("(5 + 6)", new Parenthesis(
                new Arithmetic(
                        new IntegerLiteral(5),
                        ArithmeticOperator.PLUS,
                        new IntegerLiteral(6)
                )
        ));
        success("(a + 5)");
        success("(5)");
        success("(true)");
        success("(5 / (3/2))");
        success("(5 == 6)");
        success("(5 > a)");
        success("(5 < a)");
        success("(5 != a)");
        success("(5 >= a)");
        success("(5 <= a)");

        failure("(5");
        failure("5)");
        failure("(5 + (3)");
    }

    @Test
    public void testArithmetic() {
        this.rule = parser.add_expr;
        successExpect("1 - a", new Arithmetic(
                new IntegerLiteral(1),
                ArithmeticOperator.MINUS,
                new Reference("a"))
        );
        successExpect("1 + 1 * 4", new Arithmetic(
                new IntegerLiteral(1),
                ArithmeticOperator.PLUS,
                new Arithmetic(
                        new IntegerLiteral(1),
                        ArithmeticOperator.TIMES,
                        new IntegerLiteral(4)
                )
        ));
        success("-1 + 1 - 4 * (32)");
        success("(-1 * (a - 2)) % x");
        success("a % x");
        success("[5] + [b, c, true]");

        failure("1 + 1 -");
        failure("%");
        failure("a % (x - 1");
    }

    @Test
    public void testOr() {
        this.rule = parser.or_expr;
        successExpect("true || false && 5 > 6", new Arithmetic(
                new BooleanLiteral(true),
                ArithmeticOperator.OR,
                new Arithmetic(
                        new BooleanLiteral(false),
                        ArithmeticOperator.AND,
                        new Arithmetic(new IntegerLiteral(5), ArithmeticOperator.GT, new IntegerLiteral(6))
                )
        ));
        success("a[2] && !false || 5 == 6");
        success("!(a[2] && !false && 5 == 6 * 6)");
        success("5 + 5");

        failure("true ||");
        failure("false &&");
        failure("5 > || 6");
        failure("&& 6");
        failure("|| 6");
    }

    @Test
    public void testAnd() {
        this.rule = parser.and_expr;
        successExpect("!(a[2] && !false || 5 == 6 * 6)",
                new Prefix(
                        PrefixOperator.NOT,
                        new Parenthesis(
                                new Arithmetic(
                                        new Arithmetic(
                                                new DataAccess(new Reference("a"), new IntegerLiteral(2)),
                                                ArithmeticOperator.AND,
                                                new Prefix(PrefixOperator.NOT, new BooleanLiteral(false))
                                        ),
                                        ArithmeticOperator.OR,
                                        new Arithmetic(
                                                new IntegerLiteral(5),
                                                ArithmeticOperator.EQEQ,
                                                new Arithmetic(
                                                        new IntegerLiteral(6),
                                                        ArithmeticOperator.TIMES,
                                                        new IntegerLiteral(6)
                                                )
                                        )
                                )
                        )
                )
        );
        success("!(a[2] && !false && 5 == 6 * 6)");
        success("5 + 5");

        failure("true || false && 5 > 6");
        failure("a[2] && !false || 5 == 6");
        failure("true ||");
        failure("false &&");
        failure("5 > || 6");
        failure("&& 6");
        failure("|| 6");
    }

    @Test
    public void testEquality() {
        this.rule = parser.equality_expr;
        successExpect("a[2] == !false == true",
                new Arithmetic(
                        new Arithmetic(
                                new DataAccess(new Reference("a"), new IntegerLiteral(2)),
                                ArithmeticOperator.EQEQ,
                                new Prefix(PrefixOperator.NOT, new BooleanLiteral(false))
                        ),
                        ArithmeticOperator.EQEQ,
                        new BooleanLiteral(true)
                )
        );
        success("a[2] == !false");
        success("a[2] == !false == true");
        success("a[2] == a * 5");
        success("!(a[2] == !false)");

        failure("a[2] && !false && 5 == 6 * 6");
        failure("true || false && 5 > 6");
        failure("a[2] && !false || 5 == 6");
        failure("true ||");
        failure("false &&");
        failure("5 > || 6");
        failure("&& 6");
        failure("|| 6");
    }

    @Test
    public void testPrefix() {
        this.rule = parser.prefix_expr;
        successExpect("!!true", new Prefix(
                PrefixOperator.NOT,
                new Prefix(PrefixOperator.NOT, new BooleanLiteral(true))
        ));
        success("-1");
        success("!true");
        success("!(true&&false)");
        success("!(-1>-2)");

        failure("!(");
        failure("?true");
    }

    @Test
    public void testArray() {
        this.rule = parser.array;
        successExpect("[a, 1]", new ArrayLiteral(Arrays.asList(new Reference("a"), new IntegerLiteral(1))));
        success("[]");
        success("[1]");
        success("[1, 2]");
        success("[a, 1]");
        success("[n + 1, a + 3, true, false]");
        success("[a[1], my_func(test)]");

        failure("[, 2]");
        failure("[1,]");
        failure("1, 2]");
        failure("[1, 2");
    }

    @Test
    public void testArrayAccess() {
        this.rule = parser.postfix_expr;
        successExpect("a[0]", new DataAccess(new Reference("a"), new IntegerLiteral(0)));
        success("array_access[a]");
        success("my_func()[1]");

        failure("a[0");
        failure("var[0]");
    }

    @Test
    public void testPair() {
        this.rule = parser.pair;
        successExpect("1 + 1: a", new PairLiteral(
                new Arithmetic(new IntegerLiteral(1), ArithmeticOperator.PLUS, new IntegerLiteral(1)),
                new Reference("a")
        ));
        success("a: b");
        success("a[0]: true");
        success("\"1\": true");

        failure("a");
        failure("a: b: c");
        failure("a; b");
    }

    @Test
    public void testMap() {
        this.rule = parser.map;
        successExpect("{a: b, a: 6, b:true}", new MapLiteral(
                Arrays.asList(
                        new PairLiteral(new Reference("a"), new Reference("b")),
                        new PairLiteral(new Reference("a"), new IntegerLiteral(6)),
                        new PairLiteral(new Reference("b"), new BooleanLiteral(true))
                )
        ));
        success("{1 + 1: a}");
        success("{\"a\": b}");
        success("{a[0]: true}");
        success("{\"abcd\": (a == 2), 2: my_func(a)}");

        failure("{a}");
        failure("{a: b: c}");
        failure("{a; b}");
        failure("{a: b, a: 6, b:true");
        failure("a: b, a: 6, b:true}");
        failure("{a: b ; a: 6, b:true}");
    }

    @Test
    public void testMapAccess() {
        this.rule = parser.postfix_expr;
        successExpect("map_access[\"a\"]",
                new DataAccess(
                        new Reference("map_access"),
                        new StringLiteral(List.of(new StringChunk("a")))
                )
        );
        success("a[0]");
        success("map_access[a][b]");
        success("my_func()[a]");
        success("a[\"0\"]");

        failure("a[0");
        failure("var[0]");
    }

    @Test
    public void testArg() {
        this.rule = parser.arg;
        successExpect("1", new FunctionParamCall(null, new IntegerLiteral(1)));
        success("a");
        success("a[1]");
        success("true");
        success("[true, a, 1]");

        failure("if");
        failure("ret 5");
    }

    @Test
    public void testArgs() {
        this.rule = parser.args;
        successExpect("a, 1", Arrays.asList(
                new FunctionParamCall(null, new Reference("a")),
                new FunctionParamCall(null, new IntegerLiteral(1))
        ));
        success("1");
        success("a");
        success("a[1]");
        success("true");
        success("[true, a, 1]");
        success("1, a");
        success("a, b, c, a, 1, [2]");
        success("true, false, my_func()");

        failure("if");
        failure("a 5");
        failure("1; a");
    }

    @Test
    public void testFunctionCall() {
        this.rule = parser.postfix_expr;
        successExpect("my_func(a, 1)", new FunctionCall(
                new Reference("my_func"),
                Arrays.asList(
                        new FunctionParamCall(null, new Reference("a")),
                        new FunctionParamCall(null, new IntegerLiteral(1))
                )
        ));
        successExpect("my_func()", new FunctionCall(
                new Reference("my_func"),
                Arrays.asList()
        ));
        success("my_func(1)");
        success("my_func(1, b, \"c\")");
        success("my_func(a[0], 1 + 2)");
        success("my_func(0, my_other_func(1))");

        failure("func()");
        failure("my_func(");
        failure("my_func(ret 3)");
        failure("my_func(if)");
    }
}
