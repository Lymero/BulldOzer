import AST.literals.BooleanLiteral;
import AST.literals.IntegerLiteral;
import AST.literals.StringChunk;
import norswap.autumn.AutumnTestFixture;
import org.testng.annotations.Test;

public class ParserLiteralsTests extends AutumnTestFixture {

    GrammarParser parser = new GrammarParser();

    @Test
    public void testInteger() {
        this.rule = parser.integer_literal;
        successExpect("-938742",  new IntegerLiteral(-938742));
        success("0");
        success("-1");
        success("923847");

        failure("-");
        failure("23-");
        failure("2-3");
    }

    @Test
    public void testPosInteger() {
        this.rule = parser.pos_integer_literal;
        successExpect("923847",923847);
        success("0");

        failure("-938742");
        failure("-1");
        failure("-");
        failure("23-");
        failure("2-3");
        failure("a");
        failure("_");
    }

    @Test
    public void testStringChar() {
        this.rule = parser.string_char;
        success("0");
        success("9");
        success("a");
        success("Z");
        success("\\n");
        success("\\r");
        success("_");

        failure("\u0000");
        failure("\u001F");
        failure("{");
        failure("}");
        failure("\"");
        failure("\\");
    }

    @Test
    public void testStringPart() {
        this.rule = parser.string_part;
        successExpect("this is a string", new StringChunk("this is a string"));
        success("test");
        success("   ");

        failure("{}");
        failure("{test}");
        failure("\"");
        failure("\"test\"");
    }

    @Test
    public void testBoolean() {
        this.rule = parser.boolean_literal;
        successExpect("true", new BooleanLiteral(true));
        success("true");
        success("false");

        failure("fa");
        failure("1");
        failure("tr ue");
        failure("0");
    }
}
