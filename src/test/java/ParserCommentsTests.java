import norswap.autumn.AutumnTestFixture;
import org.testng.annotations.Test;

public class ParserCommentsTests extends AutumnTestFixture {

    GrammarParser parser = new GrammarParser();

    @Test
    public void testComment() {
        this.rule = parser.comment;
        success("#");
        success("# a comment");
        success("# # comment");
        success("#  comment\n");
        success("#  comment\r\n");

        failure("# \n not comment");
        failure("# \r\n not comment");
        failure("-938742");
    }
}
