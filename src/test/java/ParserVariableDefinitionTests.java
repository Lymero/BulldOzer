import AST.expressions.*;
import AST.literals.*;
import AST.statements.ExpressionStatement;
import AST.statements.VarDeclaration;
import AST.types.Basic;
import AST.types.CompoundArray;
import AST.types.CompoundMap;
import norswap.autumn.AutumnTestFixture;
import org.testng.annotations.Test;

import java.util.Arrays;

public class ParserVariableDefinitionTests extends AutumnTestFixture {

    GrammarParser parser = new GrammarParser();

    @Test
    public void testVarDefinition() {
        this.rule = parser.var_definition;
        successExpect("var a_variable = (5 + 5)", new VarDeclaration(
                new Basic("var"),
                "a_variable",
                new Parenthesis(
                        new Arithmetic(
                                new IntegerLiteral(5),
                                ArithmeticOperator.PLUS,
                                new IntegerLiteral(5))
                )
        ));
        success("var a =  10");
        success("var a = 10 == 10");
        success("var a_variable = 10");
        success("var a_variable = 5 + 5");
        success("var a_variable = (5 + 5)");
        success("var a_variable = 10");
        success("var _still_fine = 1");
        success("var x = $i");

        failure("var var a = 10");
        failure("var a1 = 10");
        failure("# var   a  =  10");
        failure("var bad =");
        failure("var bad == 1");
        failure("a_variable = 10");
        failure("let a_variable = 10");
        failure("var else = 10");
        failure("var if = 10");
        failure("var var = 10");
        failure("$1 = 5");
    }

    @Test
    public void testVarAssignment() {
        this.rule = parser.expression_statement;
        successExpect("a = 10 == 10", new ExpressionStatement(new Assignment(
                new Reference("a"),
                new Arithmetic(new IntegerLiteral(10), ArithmeticOperator.EQEQ, new IntegerLiteral(10))
        )));
        success("a = 10");
        success("a = 10 == 10");
        success("a = a");
        success("a = b = c");
        failure("a = if (true) { false }");
    }

    @Test
    public void testArrayDefinition() {
        this.rule = parser.var_definition;
        successExpect("array<integer> i = [[1], 2, a]", new VarDeclaration(
                new CompoundArray(new Basic("integer")),
                "i",
                new ArrayLiteral(Arrays.asList(
                        new ArrayLiteral(Arrays.asList(new IntegerLiteral(1))),
                        new IntegerLiteral(2),
                        new Reference("a")
                ))
        ));
        success("var a = [1, 2, 3]");
        success("var i = [[1], 2, 3]");
    }

    @Test
    public void testArrayAssignment() {
        this.rule = parser.expression_statement;
        successExpect("i = [true == false, 2]", new ExpressionStatement(new Assignment(
                new Reference("i"),
                new ArrayLiteral(Arrays.asList(
                        new Arithmetic(
                                new BooleanLiteral(true),
                                ArithmeticOperator.EQEQ,
                                new BooleanLiteral(false)),
                        new IntegerLiteral(2)
                ))
        )));
        success("i = [1, 2, 3]");
        success("i = [1 + 2, 2, 3]");
        success("i = [true, \"2\", 3]");
        success("i = [true == false, 2, 3]");
        success("i = [a, 2, 3]");
        failure("i = [1, 2, 3");
        failure("i = 1 + 2, 2, 3]");
    }

    @Test
    public void testMapDefinition() {
        this.rule = parser.var_definition;
        successExpect("map<string, bool> a = {\"abcd\": (a == 2), 2: my_func(a)}", new VarDeclaration(
                new CompoundMap(new Basic("string"), new Basic("bool")),
                "a",
                new MapLiteral(Arrays.asList(
                        new PairLiteral(new StringLiteral(Arrays.asList(new StringChunk("abcd"))),
                                new Parenthesis(
                                        new Arithmetic(
                                                new Reference("a"),
                                                ArithmeticOperator.EQEQ,
                                                new IntegerLiteral(2)
                                        )
                                )
                        ),
                        new PairLiteral(
                                new IntegerLiteral(2),
                                new FunctionCall(
                                        new Reference("my_func"),
                                        Arrays.asList(new FunctionParamCall(null, new Reference("a")))
                                )
                        )
                ))
        ));
        success("map<string, integer> a = {\"1\" : 0, 2 : 1}");
        success("map<string, integer> a = {\"1\" : 0, \"2\" : 1}");
        success("map<string, integer> i = {\"1\" : true, 2 : \"1\"}");
        success("var a = {\"1\" : (a == 2), 2 : my_func(a)}");
        success("map<string, integer> a = { a : b }");
        success("var a = {}");

        failure("map<string, integer> a = {");
        failure("map<string, integer> i = }");
        failure("map<string, integer> a = { {} }");
    }

    @Test
    public void testMapAssignment() {
        this.rule = parser.expression_statement;
        successExpect("i = { a : b}", new ExpressionStatement(new Assignment(
                new Reference("i"),
                new MapLiteral(Arrays.asList(new PairLiteral(new Reference("a"), new Reference("b"))))
        )));
        success("a = {\"1\" : 0, 2 : 1}");
        success("a = {\"1\" : 0, \"2\" : 1}");
        success("a = {\"1\" : true, 2 : \"1\"}");
        success("a = {\"1\" : (a == 2), 2 : my_func(a)}");
        success("i = { a : b }");
        success("a = {}");

        failure("a = {");
        failure("i = }");
        failure("a = { {} }");
    }

}
