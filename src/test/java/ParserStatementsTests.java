import AST.*;
import AST.expressions.*;
import AST.literals.BooleanLiteral;
import AST.literals.IntegerLiteral;
import AST.statements.*;
import AST.types.Basic;
import norswap.autumn.AutumnTestFixture;
import org.testng.annotations.Test;

import java.util.Arrays;

public class ParserStatementsTests extends AutumnTestFixture {

    GrammarParser parser = new GrammarParser();

    @Test
    public void testBlock() {
        this.rule = parser.block;
        successExpect("{}", new Block(Arrays.asList(new Statement[]{})));
        successExpect("{ a = 5 }",
                new Block(
                        Arrays.asList(new ExpressionStatement(
                                new Assignment(
                                        new Reference("a"),
                                        new IntegerLiteral(5))
                        ))
                )
        );
        success("{ if(true) { a = 5 } }");
        success("{ a = 5 \n b = 6}");

        failure("{");
        failure("{ a = 5");
        failure("{ if(true) { { a = 5 } }");
    }

    @Test
    public void testIfElse() {
        this.rule = parser.if_statement;
        successExpect("if(true){ if (true) { x = 5 } }",
                new If(
                        new Parenthesis(new BooleanLiteral(true)),
                        new Block(Arrays.asList(new Statement[]{
                                new If(
                                        new Parenthesis(new BooleanLiteral(true)),
                                        new Block(Arrays.asList(new Statement[]{
                                                new ExpressionStatement(
                                                        new Assignment(new Reference("x"), new IntegerLiteral(5))
                                                )
                                        })),
                                        Arrays.asList(new Elif[]{}),
                                        null
                                )
                        })),
                        Arrays.asList(new Elif[]{}),
                        null
                ));
        successExpect("if(true){z=1+3}else{b=2}",
                new If(
                        new Parenthesis(new BooleanLiteral(true)),
                        new Block(Arrays.asList(new ExpressionStatement(
                                new Assignment(
                                        new Reference("z"),
                                        new Arithmetic(
                                                new IntegerLiteral(1),
                                                ArithmeticOperator.PLUS,
                                                new IntegerLiteral(3)
                                        )
                                ))
                        )),
                        Arrays.asList(new Elif[]{}),
                        new Block(Arrays.asList(new ExpressionStatement(
                                new Assignment(
                                        new Reference("b"),
                                        new IntegerLiteral(2)
                                ))
                        ))
                ));
        success("if(false){ x = 5}");
        success("if(1<3){ x = 5 }");
        success("if(true){ x = 5 }else{ x = 5 }");
        success("if(true){a=1}else{ x = 5 }");
        success("if(true){ x = 5 }else{b=2}");
        success("if(true && true){ if (true) {} }");

        failure("if() { z = 5 }");
        failure("if(true) z = 5");
        failure("if(true) { z = 5 } else z = 4");
        failure("else{ x = 5 }");
        failure("if(){ x = 5 }");
        failure("if(*4){ x = 5 }");
    }

    @Test
    public void testElif() {
        this.rule = parser.if_statement;
        successExpect("if(true) { } elif(true) { } else { a = 5 }",
                new If(
                        new Parenthesis(new BooleanLiteral(true)),
                        new Block(Arrays.asList(new Statement[]{})),
                        Arrays.asList(
                                new Elif(
                                        new Parenthesis(new BooleanLiteral(true)),
                                        new Block(Arrays.asList(new Statement[]{}))
                                )),
                        new Block(Arrays.asList(new ExpressionStatement(
                                new Assignment(
                                        new Reference("a"),
                                        new IntegerLiteral(5)
                                )
                        )))
                ));
        success("if(true) { x = 5 } else {}");
        success("if(true) { x = 5 } elif(true) { x = 5 }");
        success("if(true) { x = 5 } elif(true) { x = 5 }");
        success("if(true) { x = 5 } elif(true) { x = 5 } elif(true) { x = 5 }");
        success("if(true) { x = 5 } elif(true) { x = 5 } elif(true) { x = 5 } else { x = 5 }");
        success("if(true) { x = 5 } elif(true) { var a = 5 }");

        failure("if(true) { x = 5 } elif() { x = 5 }");
        failure("if(true) { x = 5 } elif { x = 5 }");
        failure("if(true) { x = 5 } else { x = 5 } elif(true) { x = 5 }");
        failure("elif(true) { x = 5 }");
    }

    @Test
    public void testIfElifElseConditionals() {
        this.rule = parser.if_statement;
        success("if(true && true){ x = 5 }");
        success("if(true == false){ x = 5 }");
        success("if(true != false){ x = 5 }");
        success("if(true || (1 < 4)){ x = 5 }");
        success("if(true || (true && false)){ x = 5 }");
        success("if((true) || (true && false)){ x = 5 }else{ x = 5 }");

        failure("if(){ x = 5 }");
        failure("if(true true){ x = 5 }");
        failure("if(true fail){ x = 5 }");
        failure("if(true &|& false){ x = 5 }");
        failure("if(true &&){ x = 5 }");
        failure("if(true === false){ x = 5 }");
        failure("if(true !== false){ x = 5 }");
    }

    @Test
    public void testWhileStatement() {
        this.rule = parser.while_statement;
        successExpect("while(true && true){ x = 5 }",
                new While(
                        new Parenthesis(
                                new Arithmetic(
                                        new BooleanLiteral(true),
                                        ArithmeticOperator.AND,
                                        new BooleanLiteral(true))
                        ),
                        new Block(
                                Arrays.asList(new Statement[]{
                                        new ExpressionStatement(new Assignment(new Reference("x"), new IntegerLiteral(5)))
                                })
                        )
                )
        );
        success("while(true == false){ x = 5}");
        success("while(true != false){ x = 5}");
        success("while(true || (1 < 4)){ x = 5}");
        success("while(true || (true && false)){ x = 5}");
        success("while(true) { while (true) { var a = 5 } }");

        failure("while() a = 5");
        failure("while(true) a = 5");
        failure("while(true true){ x = 5}");
        failure("while(true fail){ x = 5}");
        failure("while(true &|& false){ x = 5}");
        failure("while(true &&){ x = 5}");
        failure("while(true === false){ x = 5}");
        failure("while(true !== false){ x = 5}");
        failure("while((true) || (true && false)){ x = 5}else{ x = 5}");
    }

    @Test
    public void testParamDefinition() {
        this.rule = parser.func_param_definition;
        successExpect("integer a = 5 + 5",
                new Parameter(
                        new Basic("integer"),
                        "a",
                        new Arithmetic(
                                new IntegerLiteral(5),
                                ArithmeticOperator.PLUS,
                                new IntegerLiteral(5)
                        )
                ));
        success("integer a");
        success("bool a = b");

        failure("(a)");
        failure("a");
        failure("integer 5");
        failure("bool a == a");
        failure("bool a = ");
        failure("bool a = var");
    }

    @Test
    public void testParamsDefinition() {
        this.rule = parser.func_params_definition;
        successExpect("(integer a, bool b, string c)",
                Arrays.asList(
                        new Parameter(new Basic("integer"), "a", null),
                        new Parameter(new Basic("bool"), "b", null),
                        new Parameter(new Basic("string"), "c", null))
        );
        success("(integer a)");
        success("(integer a, bool b, string c, var d)");
        success("()");

        failure("(a)");
        failure("(5)");
        failure("(a");
        failure("a)");
    }

    @Test
    public void testSimpleReturn() {
        this.rule = parser.return_statement;
        successExpect("ret",
                new Return(Arrays.asList(new Expression[]{}))
        );
        successExpect("ret a + b",
                new Return(
                        Arrays.asList(
                                new Expression[]{
                                        new Arithmetic(
                                                new Reference("a"),
                                                ArithmeticOperator.PLUS,
                                                new Reference("b")
                                        )
                                }
                        )
                )
        );
        success("ret a");
        success("ret true");
        success("ret true || (true && false) || 5 > a");
        success("ret 0");
        success("ret a[0]");
        success("ret my_func(0)");

        failure("0");
        failure("ret ret");
        failure("ret if(true) { a = 5 }");
    }

    @Test
    public void testFunctionBlock() {
        this.rule = parser.block;
        successExpect("{ integer a = 5 \n ret a }",
                new Block(
                        Arrays.asList(
                                new VarDeclaration(
                                        new Basic("integer"),
                                        "a",
                                        new IntegerLiteral(5)
                                ),
                                new Return(
                                        Arrays.asList(new Expression[]{
                                                new Reference("a")
                                        })
                                ))
                ));
        success("{ var a = 5 }");
        success("{ if(true) { ret 5 } var a = 5 ret a}");

        failure("{ ret 5 \n var a = 5 \n ret a }");
        failure("{ var a = 5 \n ret a ret a}");
    }

    @Test
    public void testFunctionDefinition() {
        this.rule = parser.function_definition;
        successExpect("func bool parse () { x = 20 }",
                new Function(
                        new Basic("bool"),
                        "parse",
                        Arrays.asList(new Parameter[]{}),
                        new Block(Arrays.asList(new Statement[]{
                                new ExpressionStatement(new Assignment(new Reference("x"), new IntegerLiteral(20)))
                        }))
                )
        );
        successExpect("func var parse (integer a, integer b) { ret a + c }", new Function(
                new Basic("var"),
                "parse",
                Arrays.asList(
                        new Parameter(new Basic("integer"), "a", null),
                        new Parameter(new Basic("integer"), "b", null)
                ),
                new Block(Arrays.asList(
                        new Return(Arrays.asList(
                                new Arithmetic(
                                        new Reference("a"),
                                        ArithmeticOperator.PLUS,
                                        new Reference("c")
                                )
                        ))
                ))
        ));
        success("func integer parse (integer a, integer b, integer c) { ret 5 }");
        success("func bool parse (integer a, integer b, integer c) { ret a + b + c }");
        success("func string parse (integer a, integer b, integer c) { var a = 3 }");
        success("func void parse (integer a, integer b, integer c, integer e, integer f) { a = 3 }");
        success("func void parse (integer a, integer b, integer c) { func void reparse (integer a, integer b) { ret 5 } }");
        success("func void parse (integer a) { ret 5 }");

        failure("func parse (integer a) { ret 5  }");
        failure("fun void parse (integer a, integer b, integer c) { ret 5 }");
        failure("func void func void (integer a, integer b, integer c) { ret 5 }");
        failure("func void 5 (integer a, integer b, integer c) { ret 5 }");
        failure("func void parse (0, integer b, integer c) { ret 5 }");
        failure("func void parse (integer a, integer b, integer c)");
        failure("func void parse (integer a, integer b, integer c) { ret a \n a = 1 + 1 }");
        failure("func void parse (integer a, integer b, integer c) { ret a \n ret b}");
        failure("func void parse (integer a, integer b, integer c) { c = c - 1 \n ret a \n a = 1 + 1 }");
    }
}
