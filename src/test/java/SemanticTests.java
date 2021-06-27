import AST.BulldOzerNode;
import norswap.autumn.AutumnTestFixture;
import norswap.uranium.Reactor;
import norswap.uranium.UraniumTestFixture;
import norswap.utils.visitors.Walker;
import org.testng.annotations.Test;

public class SemanticTests extends UraniumTestFixture {
    private final GrammarParser parser = new GrammarParser();
    private final AutumnTestFixture autumnFixture = new AutumnTestFixture();

    {
        autumnFixture.rule = parser.root();
        autumnFixture.runTwice = false;
        autumnFixture.bottomClass = this.getClass();
    }

    @Override
    protected Object parse(String input) {
        return autumnFixture.success(input).topValue();
    }

    @Override
    protected String astNodeToString(Object ast) {
        return ast.toString();
    }

    @Override
    protected void configureSemanticAnalysis(Reactor reactor, Object ast) {
        Walker<BulldOzerNode> walker = SemanticAnalysis.createWalker(reactor);
        walker.walk(((BulldOzerNode) ast));
    }

    @Test
    public void testTypes() {
        successInput("integer x = 5");
        successInput("string x = \"5\"");
        successInput("bool x = true");
        successInput("array<integer> x = []");
        successInput("array<integer> x = [1, 2]");
        successInput("map<string, integer> x = {}");
        successInput("map<string, integer> x = {\"a\" : 5}");
        successInput("var x = 5");
        successInput("var x = \"5\"");
        successInput("var x = true");

        failureInputWith("integer x = true",
                "Required type 'integer' but got 'bool' for variable 'x'");
        failureInputWith("bool x = 5",
                "Required type 'bool' but got 'integer' for variable 'x'");
        failureInputWith("string x = 5",
                "Required type 'string' but got 'integer' for variable 'x'");
        failureInputWith("array<integer> x = [true]",
                "Required type 'array<integer>' but got 'array<bool>' for variable 'x'");
        failureInputWith("map<integer, bool> x = {true : 5}",
                "Required type 'map<integer, bool>' but got 'map<bool, integer>' for variable 'x'");
    }

    @Test
    public void testLiterals() {
        successInput("integer x = 5");
        successInput("bool x = true");
        successInput("bool x = false");
        successInput("var x = \"test\"");
        successInput("bool x = !false");
        successInput("bool x = !true");
        successInput("bool x = !!!true");
        successInput("var x = 5");
        successInput("var x = false");
        successInput("string x = \"test\"");

        failureInputWith("var x = !5",
                "Cannot apply operator 'NOT' on type 'integer'");
        failureInputWith("var x = x",
                "Cannot resolve symbol 'x'");
    }

    @Test
    public void testParenthesisExpression() {
        successInput("integer x = (1337)");
        successInput("string x = (\"abc\")");
        successInput("bool x = !(true)");
    }

    @Test
    public void testConcatenation() {
        successInput("var x = \"a\" + \"b\"");
        successInput("string x = \"a\" + \"b\"");
        successInput("string x = \"a\" + \"b\" + \"c\"");
        successInput("string x = \"a\" + \"{5}\"");

        failureInputWith("string x = \"1\" + 2",
                "Invalid arithmetic expression: string plus integer");
        failureInputWith("string x = true * \"1\"",
                "Invalid arithmetic expression: bool times string");
    }

    @Test
    public void testArithmeticExpression() {
        successInput("var x = 1 + 2");
        successInput("integer x = 1 + 2");
        successInput("integer x = 1 - 2");
        successInput("integer x = 1 * 2");
        successInput("integer x = 1 / 2");
        successInput("integer x = 1 % 2");
        successInput("integer x = 2 % 1");
        successInput("integer x = 2 % 1 + 5 * 5 - 3");

        failureInputWith("integer x = 1 + true",
                "Invalid arithmetic expression: integer plus bool");
        failureInputWith("integer x = true * true",
                "Invalid arithmetic expression: bool times bool");
        failureInputWith("integer x = 1 / true",
                "Invalid arithmetic expression: integer div bool");
    }

    @Test
    public void testNumericComparison() {
        successInput("var x = 5 > 6");
        successInput("bool x = 5 > 6");
        successInput("bool x = 5 < 6");
        successInput("bool x = 5 >= 6");
        successInput("bool x = 5 <= 6");

        failureInputWith("bool x = 5 < true",
                "Comparison on non-numeric type: integer LT bool");
        failureInputWith("bool x = true > [5]",
                "Comparison on non-numeric type: bool GT array");
    }

    @Test
    public void testEquality() {
        successInput("var x = true == true");
        successInput("bool x = true == true");
        successInput("bool x = 5 == 6");
        successInput("bool x = [5] == [6, 8]");
        successInput("bool x = [] != [5, 7]");
        successInput("bool x = \"a\" == \"b\"");
        successInput("bool x = false != true");

        failureInputWith("var x = false == 5", "Equality on incomparable types: bool integer");
        failureInputWith("var x = \"5\" != 5", "Equality on incomparable types: string integer");
    }

    @Test
    public void testVarDeclaration() {
        successInput("var x = 5");
        successInput("integer x = 5");
        successInput("bool x = true");
        successInput("string x = \"a\"");
        successInput("var x = 5 + 5 x = x + 5");
        successInput("var x = true x = x");
        successInput("var x = \"true\" x = x");
        successInput("var x = true x = x && true || x");

        failureInputWith("void x = 5",
                "Cannot declare a variable of type void");
        failureInputWith("integer x = true",
                "Required type 'integer' but got 'bool' for variable 'x'");
        failureInputWith("var x = true x = x + 5",
                "Invalid arithmetic expression: bool plus integer");
        failureInputWith("var x = true x = x + true",
                "Invalid arithmetic expression: bool plus bool");
        failureInputWith("x = x * 2",
                "Cannot resolve symbol 'x'");
        failureInputWith("x = x * 2 var x = 2",
                "Variable 'x' used before declaration");
    }

    @Test
    public void testArrayLiteral() {
        successInput("var x = [[1], []]");
        successInput("array<integer> x = []");
        successInput("array<bool> x = [true, true, false]");
        successInput("var x = []");
        successInput("array<integer> x = [1, 2, 3]");
        successInput("array<string> x = [\"a\", \"test\", \"3\"]");
        successInput("array<integer> x = [5] + [6]");
        successInput("array<integer> x = [5] + [3, 9]");
        successInput("array<integer> x = [5] + [3, 9]");
        successInput("var x = [3, 9] + [5]");
        successInput("var x = [[3], [9]] + [[5]]");
        successInput("array<integer> x = [] + [5]");
        successInput("array<integer> x = [5] + []");
        successInput("integer a = 5 array<integer> x = [a, 5, 3]");
        successInput("" +
                "array<integer> a = [3, 9] + [5] " +
                "var b = [9, 8] " +
                "array<integer> c = a + b");

        failureInputWith("array<integer> x = [true]",
                "Required type 'array<integer>' but got 'array<bool>' for variable 'x'");
        failureInputWith("var x = [1, true]",
                "Cannot have different types in a array");
        failureInputWith("var x = [[1], [true]]",
                "Cannot have different types in a array");
        failureInputWith("var x = [5] + [true]",
                "Cannot append type 'array<integer>' to type 'array<bool>");
        failureInputWith("var x = [[3], [9]] + [[true]]",
                "Cannot append type 'array<array<integer>>' to type 'array<array<bool>>'");
        failureInputWith("integer x = []",
                "Required type 'integer' but got 'array<unknown>' for variable 'x'");
        failureInputWith("var a = [1, 2] a = a + 5",
                "Invalid arithmetic expression: array<integer> plus integer");
        failureInputWith("var x = [] + 5",
                "Invalid arithmetic expression: array<unknown> plus integer");
        failureInputWith("var x = [7] - [5]",
                "Invalid arithmetic expression: array<integer> minus array<integer>");
        failureInputWith("" +
                        "var a = [3, 9] + [5] " +
                        "var b = [true] " +
                        "var c = a + b",
                "Cannot append type 'array<integer>' to type 'array<bool>'");
    }

    @Test
    public void testArrayAccess() {
        successInput("var a = [1, 2] integer x = a[0]");
        successInput("var a = [1, 2] + [] integer x = a[0]");
        successInput("var a = [] + [1, 2] integer x = a[0]");
        successInput("var a = [true, true] bool x = x[0]");
        successInput("var a = [\"1\", \"2\"] string x = a[0]");
        successInput("var a = [[1], [2]] array<integer> x = a[0]");
        successInput("var a = [1] var x = 1 integer b = a[x] ");
        successInput("var a = [1] var x = a[parseToInt(\"1\")]");
        successInput("var a = [\"1\"] print(a[0])");
        successInput("var a = [1, 2] a[0] = 3");

        failureInputWith("var a = [1, 2] bool x = a[0]",
                "Required type 'bool' but got 'integer' for variable 'x'");
        failureInputWith("var a = [1, 2] bool x = a[1]",
                "Required type 'bool' but got 'integer' for variable 'x'");
        failureInputWith("var a = [[1], [2]] array<array<integer>> x = a[1] + [1]",
                "Required type 'array<array<integer>>' but got 'array<integer>' for variable 'x'");
        failureInputWith("var a = [1] var x = a[true]",
                "Cannot access type 'array<integer>' with key of type 'bool'");
        failureInputWith("var a = [1] var x = a[\"1\"]",
                "Cannot access type 'array<integer>' with key of type 'string'");
        failureInputWith("var a = [1, 2] a[0] = true",
                "Cannot assign type 'bool' to type 'integer'");
    }

    @Test
    public void testMapLiteral() {
        successInput("map<integer, bool> x = {}");
        successInput("var x = {}");
        successInput("var x = {} + {}");
        successInput("map<integer, integer> x = {5:5, 1:1}");
        successInput("map<string, integer> x = {\"5\":5, \"1\":1}");
        successInput("var v = true map<bool, bool> x = {v:v, true:true}");
        successInput("func var f() { ret true } var x = {f: f}");
        successInput("var x = {true : [0], false : [1]}");
        successInput("var x = {true : [1, 3], false : [2, 4]} integer a = x[true][0]");
        successInput("var x = {true : {\"a\" : 5}, false : {\"a\" : 5}} integer a = x[false][\"a\"]");

        failureInputWith("var x = {true : 5, 5 : true}",
                "Cannot have different types in a map");
        failureInputWith("var x = {true : 5, true : true}",
                "Cannot have different types in a map");
        failureInputWith("var x = {true : 5, \"true\" : 5}",
                "Cannot have different types in a map");
        failureInputWith("var x = {true : [0], false : [true]}",
                "Cannot have different types in a map");
        failureInputWith("map<integer, bool> x = {true : 5}",
                "Required type 'map<integer, bool>' but got 'map<bool, integer>' for variable 'x'");
        failureInputWith("map<integer, bool> x = 5",
                "Required type 'map<integer, bool>' but got 'integer' for variable 'x'");
        failureInputWith("map<integer, bool> x = []",
                "Required type 'map<integer, bool>' but got 'array<unknown>' for variable 'x'");
        failureInputWith("var x = {true : {\"a\" : 5}, false : {\"a\" : 5}} integer a = x[false][5]",
                "Cannot access map of type 'map<string, integer>' with key of type 'integer'");
    }

    @Test
    public void testMapAccess() {
        successInput("map<integer, bool> x = {5 : true} bool x = x[5]");
        successInput("var x = {5 : true} bool x = x[5]");
        successInput("var x = {true : 5} bool x = x[true]");
        successInput("map<bool, integer> x = {true : 5} integer x = x[5]");
        successInput("var x = {\"a\" : [1, 2]} integer a = x[\"a\"][1]");
        successInput("var x = {5 : true} x[5] = true");

        failureInputWith("map<integer, bool> x = {5 : true} integer a = x[5]",
                "Required type 'integer' but got 'bool' for variable 'a'");
        failureInputWith("var x = {5 : true} integer a = x[5]",
                "Required type 'integer' but got 'bool' for variable 'a'");
        failureInputWith("var x = {5 : true} var a = x[true]",
                "Cannot access map of type 'map<integer, bool>' with key of type 'bool'");
        failureInputWith("var x = {\"a\" : [1, 2]} integer a = x[\"a\"][true]",
                "Cannot access type 'array<integer>' with key of type 'bool'");
        failureInputWith("var x = {5 : true} x[5] = 5",
                "Cannot assign type 'integer' to type 'bool'");
    }

    @Test
    public void testAssignment() {
        successInput("var x = 5 x = x + 6");
        successInput("var x = true x = !x");
        successInput("var x = [] var x = [5] integer i = x[0]");
        successInput("" +
                "func bool fun(var a) { " +
                "   ret true " +
                "} " +
                "var f = fun " +
                "f(5)");

        failureInputWith("a = 5",
                "Cannot resolve symbol 'a'");
        failureInputWith("var x = true integer y = x",
                "Required type 'integer' but got 'bool' for variable 'y'");
        failureInputWith("var x = 5 x = true",
                "Cannot assign type 'bool' to type 'integer'");
        failureInputWith("var x = 5 x = x + true",
                "Invalid arithmetic expression: integer plus bool");
        failureInputWith("fun(5) = true",
                "Cannot resolve symbol 'fun'");
        failureInputWith("func integer fun(integer a) { ret 5 } fun = 5",
                "Cannot assign type 'integer' to type 'func -> integer'");
        failureInputWith("5 + 5 = 5",
                "Final left-side value");
        failureInputWith("(1, 2) = 5",
                "Final left-side value");
        failureInputWith("[1, 2] = 5",
                "Final left-side value");
        failureInputWith("{1:1} = 5",
                "Final left-side value");
        failureInputWith("true = 5",
                "Final left-side value");
        failureInputWith("\"string\" = 5",
                "Final left-side value");
        failureInputWith("\"{5}\" = 5",
                "Final left-side value");
    }

    @Test
    public void testScope() {
        successInput("print(\"5\")");
        successInput("integer i = parseToInt(\"5\")");
        successInput("string s = parseToString(5)");
        successInput("" +
                "var x = 5" +
                "func void f() {" +
                "   integer b = x" +
                "}");

        failureInputWith("" +
                        "func void f() {" +
                        "   integer b = x" +
                        "}",
                "Cannot resolve symbol 'x'");
        failureInputWith("" +
                        "var x = true " +
                        "func void f() {" +
                        "   integer b = x" +
                        "}",
                "Required type 'integer' but got 'bool' for variable 'b'");
        failureInputWith("unknownFunction()",
                "Cannot resolve symbol 'unknownFunction'");
    }

    @Test
    public void testFunctionDeclaration() {
        successInput("func integer my_func(integer b) { ret b }");
        successInput("func integer my_func() { ret 5 }");
        successInput("func void my_func(integer b) { b = 5 }");
        successInput("func void my_func(integer b) { integer x = b }");
        successInput("func void my_func(bool b) { bool x = b }");
        successInput("func void my_func(integer b, integer x) { x = b }");
        successInput("func bool f(array<bool> x) { ret x[0] }");
        successInput("func array<bool> f(array<bool> x) { ret x }");
        successInput("func var f(array<bool> x) { ret x }");
        successInput("func map<bool, string> f(map<bool, string> x) { ret x }");

        failureInputWith("func void my_func(bool b) { integer x = b }",
                "Required type 'integer' but got 'bool' for variable 'x'");
        failureInputWith("func array<bool> f(array<string> x) { ret x }",
                "Incompatible types: required 'array<bool>' but got 'array<string>' for function 'f'");
        failureInputWith("func void my_func(integer b, bool x) { x = b }",
                "Cannot assign type 'integer' to type 'bool'");
        failureInputWith("func bool f(array<integer> x) { ret x[0] }",
                "Incompatible types: required 'bool' but got 'integer' for function 'f'");
        failureInputWith("func map<bool, string> f(map<string, bool> x) { ret x }",
                "required 'map<bool, string>' but got 'map<string, bool>' for function 'f'");
        failureInputWith("func void my_func(tuple<bool> b) { b[0] = 5 }",
                "Final left-side value");
    }

    @Test
    public void testFunctionCall() {
        successInput("print(\"abc\")");
        successInput("integer x = parseToInt(\"1\") x = x + 5");
        successInput("var x = parseToInt integer i = x(\"1\")");
        successInput("func void test() { print(\"a\") } var x = test x()");
        successInput("func integer my_func() { ret 5 } " + "my_func()");
        successInput("func var my_func() { ret 5 } integer x = my_func()");
        successInput("func void f(array<bool> x) { } f([true])");
        successInput("func var f() { ret [true] } array<bool> ab = f()");

        failureInputWith("func void f(array<bool> x) { } f([1])",
                "Expected argument at position '0' of type 'array<bool>' but got 'array<integer>'");
        failureInputWith("func integer my_func() { ret 5 } var x = my_fun()",
                "Cannot resolve symbol 'my_fun'");

        failureInputWith("func void test() { print(\"a\") } var x = test x(5)",
                "More parameters than expected were given");
        failureInputWith("var x = parseToInt integer i = x(true)",
                "Expected argument at position '0' of type 'string' but got 'bool'");
        failureInputWith("integer x = print(\"abc\")",
                "Required type 'integer' but got 'void' for variable 'x'");
        failureInputWith("var f = 5 var x = f(5)",
                "Function call expected: cannot call 'f' of type 'integer'");
        failureInputWith("func var f() { ret [true] } array<string> ab = f()",
                "Required type 'array<string>' but got 'array<bool>' for variable 'ab'");
        failureInputWith("print()",
                "Less parameters than expected were given");
    }

    @Test
    public void testReturn() {
        successInput("func integer my_func() { ret 5 }");
        successInput("func var my_func() { ret 5 }");
        successInput("func bool my_func() { ret !true || false }");
        successInput("func var my_func() { ret !true || false }");
        successInput("func array<integer> my_func() { ret [5] }");
        successInput("func string my_func() { ret \"[] + [5]\" }");
        successInput("func void my_func() { var x = [] }");
        successInput("func var my_func() { var i = 0 while (i < 10) { if (i == 0) { ret true } } } bool b = my_func()");
        successInput("func bool f() { if (true) { ret true } else { ret false }}");
        successInput("" +
                "func var my_func() { " +
                "   func bool second_func() { " +
                "       ret true " +
                "   } " +
                "   ret second_func " +
                "}" +
                "var x = my_func()" +
                "bool b = x()");

        failureInputWith("func integer my_func() { }",
                "Expected a return statement but found none");
        failureInputWith("func integer my_func() { ret }",
                "Expected a non-empty return statement");
        failureInputWith("func integer my_func() { ret true }",
                "Incompatible types: required 'integer' but got 'bool' for function 'my_func'");
        failureInputWith("func void my_func() { ret true }",
                "Incompatible types: required 'void' but got 'bool' for function 'my_func'");
        failureInputWith("func bool f() { if (true) { ret true } else { ret 1 }}",
                "Incompatible types: required 'bool' but got 'integer' for function 'f'");
        failureInputWith("func var my_func() { var i = 0 while (i < 10) { if (i == 0) { ret true } } } string b = my_func()",
                "Required type 'string' but got 'bool' for variable 'b'");
    }

    @Test
    public void testIfStatement() {
        successInput("if(true) {}");
        successInput("if(true) { var x = 5 }");
        successInput("if(true && false) {}");
        successInput("if(5 == 5) {}");
        successInput("var b = true if(b) {}");
        successInput("if(true) {} else {}");
        successInput("var x = 5 if(true) { x = 6 } else { x = 7 }");
        successInput("if(true) { if (false) {} }");
        successInput("if(true) { if (false) {} else {} } else {}");
        successInput("if(true) {} elif(true) {} else {}");
        successInput("if(true) {} elif(true) { if (true) {} } else {}");

        failureInputWith("if(1 + 1) {}",
                "Expected condition of type 'bool' but got 'integer'");
        failureInputWith("if(true) { ret 5 }",
                "Cannot return outside of a function");
    }

    @Test
    public void testWhileStatement() {
        successInput("while(true) {}");
        successInput("while(true) { var x = 5 }");
        successInput("while(true && false) {}");
        successInput("while(5 == 5) {}");
        successInput("var b = true while(b) {}");
        successInput("while(true) { while (false) {} }");
        successInput("var i = 0 while(i < 10) { i = i + 1 }");

        failureInputWith("while(1 + 1) {}",
                "Expected condition of type 'bool' but got 'integer'");
        failureInputWith("while(true) { ret 5 }",
                "Cannot return outside of a function");
        failureInputWith("var i = true while(i < 10) {}",
                "Comparison on non-numeric type: bool LT integer");
        failureInputWith("var i = true while(i < 10) { i = i + 1 }",
                "Comparison on non-numeric type: bool LT integer",
                "Invalid arithmetic expression: bool plus integer");
    }

    @Test
    public void testArgv() {
        successInput("var x = $1");
        successInput("string x = $1");
        successInput("integer x = parseToInt($1)");

        failureInputWith("string x = $true",
                "Can only access '$' with index of type integer");
        failureInputWith("string x = $a",
                "Cannot resolve symbol 'a'");
    }

    @Test
    public void testArgc() {
        successInput("var x = $@");
        successInput("integer x = $@");

        failureInputWith("string x = $@",
                "Required type 'string' but got 'integer' for variable 'x'");
        failureInputWith("$@= 5",
                "Final left-side value");
    }

    @Test
    public void testStringInterpolation() {
        successInput("string s = \"It is worth 5 dollars\"");
        successInput("string s = \"It is worth {5} dollars\"");
        successInput("string s = \"It is worth {5 + 5} dollars\"");
        successInput("string s = \"It is worth {\"5\"} dollars\"");
        successInput("string s = \"It is worth {\"{5}\"} dollars\"");

        failureInputWith("string s = \"It is worth {x} dollars\"",
                "Cannot resolve symbol 'x'");
        failureInputWith("string s = \"It is worth {\"{x}\"} dollars\"",
                "Cannot resolve symbol 'x'");
    }

    @Test
    public void testTuple() { // note: parser forbids empty tuple
        successInput("tuple<integer> t = (1, 3)");
        successInput("tuple<bool> t = (true, false, true, false)");
        successInput("integer x = 5 integer y = 6 tuple<integer> coordinates = (x, y)");
        successInput("var t = (1, 3)");
        successInput("var t = (1, 3) integer x = t[0]");

        failureInputWith("tuple<integer> t = (1, true)",
                "Cannot have different types in a tuple");
        failureInputWith("tuple<bool> t = (1, 3)",
                "Required type 'tuple<bool>' but got 'tuple<integer>' for variable 't'");
        failureInputWith("integer x = 5 integer y = 6 tuple<bool> coordinates = (x, y)",
                "Required type 'tuple<bool>' but got 'tuple<integer>' for variable 'coordinates'");
        failureInputWith("var t = (1, 3) t[0] = 1",
                "Final left-side value");
    }

    @Test
    public void testForIn() {
        successInput("for (x : [1, 2, 3]) {}");
        successInput("for (x : [1, 2, 3]) { print(x) }");
        successInput("for (x : [1, 2, 3]) { integer y = x }");
        successInput("var arr = [1, 2, 3] for (x : arr) { integer y = x }");
        successInput("for (x : [[1, 2], [3, 4]]) { for (y : x) { print(y) } }");
        successInput("for (x : [1, 2, 3]) { x = 5 }");

        failureInputWith("for (x : [1, true, 3]) {}",
                "Cannot have different types in a array");
        failureInputWith("for (x : y) {}",
                "Cannot resolve symbol 'y'");
        failureInputWith("for (x : 5) {}",
                "Unexpected type 'integer' found in for-in statement");
        failureInputWith("for (x : {1 : 1}) {}",
                "Unexpected type 'map<integer, integer>' found in for-in statement");
        failureInputWith("for (x : (1, 2, 3)) {}",
                "Unexpected type 'tuple<integer>' found in for-in statement");
    }

    @Test
    public void testNamedParameter() {
        successInput("func void f(integer x) { } f(x : 5)");
        successInput("func void f(integer x, bool y) { } f(x : 5, y : true)");
        successInput("func void f(integer x, bool y) { } f(y : true, x : 5)");
        successInput("func void f(integer x, bool y, integer z) { } f(y : true, z : 3, x : 1)");

        failureInputWith("func void f(integer x, bool y) { } f(y : true)",
                "Parameter 'x' cannot be found");
        failureInputWith("func void f(integer x, integer y) { } f(x : 8, 5)",
                "Either all parameters are named or none");
        failureInputWith("func void f(integer x, integer y) { } f(8, y : 5)",
                "Either all parameters are named or none");
        failureInputWith("func void f(integer x, bool y) { } f(y : true, z : 5)",
                "Parameter 'x' cannot be found");
        failureInputWith("func void f(integer x, bool y) { } f(y : true, y : false)",
                "Duplicate parameter name 'y'");
        failureInputWith("func void f(integer x) { } f(x : true)",
                "Expected argument at position '0' of type 'integer' but got 'bool'");
        failureInputWith("func void f(integer x) { } f(x : 5, y : 9)",
                "More parameters than expected were given");
        failureInputWith("func void f(integer x) { } f()",
                "Less parameters than expected were given");
    }

    @Test
    public void testDefaultParameter() {
        successInput("func integer f(integer x = 5) { ret x } f()");
        successInput("func integer f(integer x = 5) { ret x } f(6)");
        successInput("func integer f(integer x = 5, bool y, integer z = 6) { ret x + z } f(y : true)");
        successInput("func integer f(integer x = 5, bool y = true, integer z = 6) { ret x + z } f()");
        successInput("func var f(integer x = 5, bool y, integer z = 6) { ret x + z }");
        successInput("func void f(bool x = false, bool y) { } f(y : true)");

        failureInputWith("func integer f(integer x = 5, integer z = 6) { ret x + z } f(5)",
                "Cannot use default parameters with non-named parameters");
        failureInputWith("func integer f(integer x = 5, bool y, integer z = 6) { ret x + z } f()",
                "Less parameters than expected were given");
        failureInputWith("func integer f(integer x = 5, bool y, integer z = 6) { ret x + z } f(x : 5, true, 5)",
                "Either all parameters are named or none");
        failureInputWith("func void f(integer x = true) { }",
                "Default parameter type 'bool' does not match the expected type 'integer'");
        failureInputWith("func void f(bool x = false, bool y) { } f(y : true, 5)",
                "Either all parameters are named or none");
    }

    @Test
    public void testFibonacci() {
        successInput("func void fibonacci(integer a, integer b, integer N) {\n" +
                "    if (N == 0) { ret }\n" +
                "    print(a)\n" +
                "    fibonacci(b, a+b, N-1)\n" +
                "}\n" +
                "\n" +
                "integer N = parseToInt($1)\n" +
                "fibonacci(0, 1, N)");
    }

    @Test
    public void testFizzBuzz() {
        successInput("integer i = 1\n" +
                "while (i <= 100) {\n" +
                "    if (i % 15 == 0) {\n" +
                "        print(\"FizzBuzz\")\n" +
                "    }\n" +
                "    elif (i % 3 == 0) {\n" +
                "        print(\"Fizz\")\n" +
                "    }\n" +
                "    elif (i % 5 == 0) {\n" +
                "            print(\"Buzz\")\n" +
                "    }\n" +
                "    else {\n" +
                "        print(i)\n" +
                "    }\n" +
                "    i = i + 1\n" +
                "}\n");
    }

    @Test
    public void testPrime() {
        successInput("func bool isPrime(integer number) {\n" +
                "    if (number <= 1) {\n" +
                "        ret false\n" +
                "    }\n" +
                "    bool prime = true\n" +
                "    integer i = 2\n" +
                "    while (i < number && prime) {\n" +
                "        if (number % i == 0) {\n" +
                "            prime = false\n" +
                "        }\n" +
                "        i = i + 1\n" +
                "    }\n" +
                "    ret prime\n" +
                "}\n" +
                "\n" +
                "# MAIN\n" +
                "integer N = parseToInt($0)\n" +
                "integer current = 2\n" +
                "integer count = 0\n" +
                "\n" +
                "while (count < N) {\n" +
                "    if (isPrime(current)) {\n" +
                "        print(current)\n" +
                "        count = count + 1\n" +
                "    }\n" +
                "    current = current + 1\n" +
                "}\n");
    }

    @Test
    public void testSort() {
        successInput("func void swap(array<integer> a, integer i, integer j) {\n" +
                "    integer tmp = a[i]\n" +
                "    a[i] = a[j]\n" +
                "    a[j] = tmp\n" +
                "}\n" +
                "\n" +
                "func void sort(array<integer> numbers) {\n" +
                "    integer i = 0\n" +
                "    while (i < len(numbers)) {\n" +
                "        integer j = i+1\n" +
                "        while (j < len(numbers)) {\n" +
                "            if (numbers[i] > numbers[j]) {\n" +
                "                swap(numbers, i, j)\n" +
                "            }\n" +
                "            j = j + 1\n" +
                "        }\n" +
                "        i = i + 1\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "# MAIN\n" +
                "array<integer> numbers = []\n" +
                "integer i = 0\n" +
                "while (i < $@) {\n" +
                "    numbers[i] = parseToInt($i)\n" +
                "    i = i + 1\n" +
                "}\n" +
                "sort(numbers)\n" +
                "i = 0\n" +
                "while (i < len(numbers)) {\n" +
                "    print(parseToString(numbers[i]))\n" +
                "    i = i + 1\n" +
                "}\n");
    }

    @Test
    public void testUniq() {
        successInput("map<string, bool> m = {}\n" +
                "integer i = 0\n" +
                "while (i < $@) {\n" +
                "    if (m[$i] == unknown) {\n" +
                "        print($i)\n" +
                "        m[$i] = true\n" +
                "    }\n" +
                "    i = i + 1\n" +
                "}\n");
    }

    @Test
    public void testAdditionalFeatures() {
        successInput("# This program is used to show our additional features.\n" +
                "# It features the following features (explained in the document):\n" +
                "# - Default parameters\n" +
                "# - Named parameters\n" +
                "# - Type inference with the keyword \"var\"\n" +
                "# - For in loop\n" +
                "# - String interpolation\n" +
                "# - Tuple\n" +
                "# - String concatenation\n" +
                "# - Array concatenation\n" +
                "# - Functions as values\n" +
                "# - Nested functions\n" +
                "\n" +
                "# function that has a default parameter\n" +
                "func var look_up_value(array<integer> arr = [1, 2, 3, 4, 5, 6], integer to_find) {\n" +
                "    # \"var\" type inference, \"index\" is actually an integer\n" +
                "    var index = 0\n" +
                "    for (element : arr) { # for in loop\n" +
                "        if (element == to_find) {\n" +
                "            print(\"The element {element} has been found.\") # string interpolation\n" +
                "            ret index\n" +
                "        }\n" +
                "        index = index + 1\n" +
                "    }\n" +
                "    # type inference for the return of the function\n" +
                "    ret -1\n" +
                "}\n" +
                "\n" +
                "# named parameter + use the default parameter\n" +
                "integer first_found = look_up_value(to_find: 5)\n" +
                "\n" +
                "# \"var\" type inference\n" +
                "var second_found = look_up_value([10, 11, 12], 13)\n" +
                "\n" +
                "# tuple + type inference\n" +
                "var my_tuple = (first_found, second_found)\n" +
                "integer retrieved_first_index = my_tuple[0]\n" +
                "# my_tuple[0] = 1 # not possible, tuples are immutable\n" +
                "\n" +
                "var concatenation_array = [1, 2, 3, 4] + [5, 6] + [7, 8, 9, 10]\n" +
                "array<integer> concatenation_array_bis = [1, 2, 3, 4] + [5, 6] + [7, 8, 9, 10]\n" +
                "# type of the elements inside the array can be inferred too\n" +
                "integer x = concatenation_array[0]\n" +
                "string string_concatenation = \"Hello\" + \" \" + \"world.\"\n" +
                "\n" +
                "# returns a function using the var keyword to infer the type of the function\n" +
                "func var create_function_string_concat(string s) {\n" +
                "    func string own_string_concat(integer x = 1337) {\n" +
                "        ret s + \"{x}\"\n" +
                "    }\n" +
                "    ret own_string_concat\n" +
                "}\n" +
                "\n" +
                "var my_string_concat = create_function_string_concat(\"Super string -> \")\n" +
                "string s = my_string_concat(5)\n" +
                "string s = my_string_concat() # use the default parameter");
    }
}