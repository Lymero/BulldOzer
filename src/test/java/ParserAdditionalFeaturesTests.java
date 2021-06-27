import AST.*;
import AST.expressions.*;
import AST.literals.*;
import AST.statements.*;
import AST.types.Basic;
import norswap.autumn.AutumnTestFixture;
import org.testng.annotations.Test;

import java.util.Arrays;

public class ParserAdditionalFeaturesTests extends AutumnTestFixture {

    GrammarParser parser = new GrammarParser();

    @Test
    public void testStringInterpolation() {
        this.rule = parser.string_interpolation;
        successExpect("{1}", new StringInterpolation(new IntegerLiteral(1)));
        success("{a}");
        success("{a + 1 + a[5]}");

        failure("{}");
        failure("{a} {b}");
        failure("{a");
        failure("a}");
    }

    @Test
    public void testStringLitInterpolation() {
        this.rule = parser.string_literal;
        successExpect("\"\"",
                new StringLiteral(Arrays.asList())
        );
        successExpect("\"abc\"",
                new StringLiteral(Arrays.asList(new StringChunk("abc")))
        );
        successExpect("\"a{b}c\"",
                new StringLiteral(Arrays.asList(
                        new StringChunk("a"),
                        new StringInterpolation(new Reference("b")),
                        new StringChunk("c")
                ))
        );
        success("\"a{b}{c}\"");
        success("\"{b}\"");
        success("\"{my_str} 2\"");

        failure("\"{my_str 2");
        failure("{my_str 2\"");
        failure("\"{my_str 2\"");
        failure("\"my_str} 2\"");
        failure("\"{my str} 2\"");
        failure("\"{}\"");
    }

    @Test
    public void testTuple() {
        this.rule = parser.tuple;
        successExpect("(1, 3)",
                new TupleLiteral(
                        Arrays.asList(
                                new IntegerLiteral(1),
                                new IntegerLiteral(3)
                        )
                ));
        success("(1, test)");
        success("(it, will, succeed)");

        failure("(test");
        failure("test)");
        failure("()");
    }

    @Test
    public void testMultipleReturn() {
        this.rule = parser.return_statement;
        successExpect("ret a, b + 5, c[3]",
                new Return(
                        Arrays.asList(
                                new Reference("a"),
                                new Arithmetic(
                                        new Reference("b"),
                                        ArithmeticOperator.PLUS,
                                        new IntegerLiteral(5)
                                ),
                                new DataAccess(new Reference("c"), new IntegerLiteral(3))
                        )
                )
        );
        success("ret");
        success("ret a, b");
        success("ret a, b, c");
        success("ret true, c[\"a\"], c");

        failure("ret 1,");
        failure("ret ,1");
        failure("ret a,b,");
        failure("ret a,,b");
    }

    @Test
    public void testForInStatement() {
        this.rule = parser.for_in_statement;
        successExpect("for (x : [1, 2, 3]) {}",
                new ForIn(
                        new ForInCursor("x", new ArrayLiteral(Arrays.asList(
                                new IntegerLiteral(1),
                                new IntegerLiteral(2),
                                new IntegerLiteral(3)
                            ))
                        ),
                        new Block(Arrays.asList(new Statement[]{}))
                )
        );
        success("for (x : arr) {}");
        success("for (x : get_arr()) {}");

        failure("for (1 : [1, 2, 3]) {}");
        failure("for (x :: [1, 2, 3]) {}");
        failure("for (x in [1, 2, 3]) {}");
    }

    @Test
    public void testFunctionCallNamedParameters() {
        this.rule = parser.postfix_expr;
        successExpect("my_func(var_name: 1)",
                new FunctionCall(
                        new Reference("my_func"),
                        Arrays.asList(new FunctionParamCall("var_name", new IntegerLiteral(1)))
                ));
        success("my_func(1, 2, 3)");
        success("my_func(var_name: a == 5)");
        success("my_func(var_name: a + 5)");
        success("my_func(var_name: \"a\")");
        success("my_func(var_name: a[0], 1 + 2)");
        success("my_func(var_name_one: my_func(), var_name_two: a)");
        success("my_func()");

        failure("func()");
        failure("my_func(");
        failure("my_func(ret 3)");
        failure("my_func(if)");
        failure("my_func(var_name:)");
        failure("my_func(var_name::5)");
    }

    @Test
    public void testFunctionDeclarationDefaultParameters() {
        this.rule = parser.function_definition;
        successExpect("func integer parse (integer a = 5, integer b = 7, integer c = 9) { }",
                new Function(
                        new Basic("integer"),
                        "parse",
                        Arrays.asList(
                                new Parameter(new Basic("integer"), "a", new IntegerLiteral(5)),
                                new Parameter(new Basic("integer"), "b", new IntegerLiteral(7)),
                                new Parameter(new Basic("integer"), "c", new IntegerLiteral(9))
                        ),
                        new Block(Arrays.asList())
                ));
        success("func bool parse (integer a = my_func(), bool b = \"a\", bool c) { ret a + b + c }");
        success("func bool parse (var a, var b = true, bool c) { " +
                "func integer reparse (var a = c, integer b = -1) {} " +
                "}");
        success("func bool parse (integer a = 7 + 5) { }");
        success("func integer parse (bool a = 7 > 5) { }");
        success("func string parse (integer a = 5) { }");

        failure("func bool parse (bool a == 5) { }");
        failure("func integer parse (bool a > 5) { }");
        failure("func parse (a = 5) { }");
    }
}
