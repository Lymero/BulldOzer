import AST.*;
import AST.expressions.Arithmetic;
import AST.expressions.ArithmeticOperator;
import AST.expressions.Parenthesis;
import AST.expressions.Reference;
import AST.literals.ArgcLiteral;
import AST.literals.ArgvLiteral;
import AST.literals.IntegerLiteral;
import AST.statements.If;
import norswap.autumn.AutumnTestFixture;
import org.testng.annotations.Test;

import java.util.Arrays;

public class ParserArgsTests extends AutumnTestFixture {

    GrammarParser parser = new GrammarParser();

    @Test
    public void testArgv() {
        this.rule = parser.argv_literal;
        successExpect("$ab", new ArgvLiteral(new Reference("ab")));
        successExpect("$1", new ArgvLiteral(new IntegerLiteral(1)));
        success("$i");
        success("$10");

        failure("$");
        failure("$@");
        failure("$$");
        failure("i");
    }

    @Test
    public void testArgc() {
        this.rule = parser.argc_literal;
        successExpect("$@", new ArgcLiteral("@"));

        failure("@");
        failure("$");
        failure("$1");
        failure("$@@");
        failure("$$@@");
    }

    @Test
    public void testArgLiteral() {
        this.rule = parser.arg_literal;
        successExpect("$ab", new ArgvLiteral(new Reference("ab")));
        success("$i");
        success("$1");
        success("$10");
        success("$ab");
        success("$@");
        success("$-1");

        failure("@");
        failure("$");
        failure("$@@");
        failure("$$@@");
        failure("$");
        failure("$$");
        failure("i");
    }

    @Test
    public void testArguments() {
        this.rule = parser.statement;
        successExpect("if($@ < $0) {}",
                new If(
                        new Parenthesis(
                                new Arithmetic(
                                        new ArgcLiteral("@"),
                                        ArithmeticOperator.LT,
                                        new ArgvLiteral(new IntegerLiteral(0)))
                        ),
                        new Block(Arrays.asList()),
                        Arrays.asList(),
                        null
                )
        );
        success("if($@ < 0) {  b = $1 }");
        success("if($@ < $0) { a = my_func[$1] ret $2 }");

        failure("if($@ < 0) { var b = $ }");
        failure("if($@ < 0) { var $1 = b }");
    }
}
