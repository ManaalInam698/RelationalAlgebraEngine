import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TokenizerParserTest {

    // Tokenizes and parses a query
    private ParseNode parse(String query) {
        Tokenizer tokenizer = new Tokenizer(query);
        Parser parser = new Parser(tokenizer.tokenize());
        return parser.parse();
    }

    // Converts a parse tree to text so two trees can be compared
    private String treeToString(ParseNode tree) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try {
            System.setOut(new PrintStream(output));
            tree.printTree();
        } finally {
            System.setOut(originalOutput);
        }

        return output.toString();
    }

    // CASE 1
    // No whitespace: x1, =, and 3 must be recognized separately
    @Test
    public void testCase1NoWhitespace() {
        Tokenizer tokenizer =
                new Tokenizer("select[x1=3](R)");

        List<Token> tokens = tokenizer.tokenize();

        assertEquals("select", tokens.get(0).getValue());
        assertEquals("[", tokens.get(1).getValue());
        assertEquals("x1", tokens.get(2).getValue());
        assertEquals("=", tokens.get(3).getValue());
        assertEquals("3", tokens.get(4).getValue());

        assertDoesNotThrow(() ->
                parse("select[x1=3](R)"));
    }

    // CASE 2
    // Whitespace must not change the parse tree
    @Test
    public void testCase2WhitespaceSameTree() {
        ParseNode tree1 =
                parse("select[x1=3](R)");

        ParseNode tree2 =
                parse("select[ x1 = 3 ](R)");

        assertEquals(
                treeToString(tree1),
                treeToString(tree2)
        );
    }

    // CASE 3
    // >= must be one token
    @Test
    public void testCase3GreaterThanOrEqual() {
        Tokenizer tokenizer =
                new Tokenizer("select[Age>=30](R)");

        List<Token> tokens = tokenizer.tokenize();

        assertTrue(tokens.stream().anyMatch(
                token -> token.getValue().equals(">=")
        ));

        assertFalse(tokens.stream().anyMatch(
                token -> token.getValue().equals(">")
        ));

        assertDoesNotThrow(() ->
                parse("select[Age>=30](R)"));
    }

    // CASE 4
    // > and -30 must be separate tokens
    @Test
    public void testCase4NegativeNumber() {
        Tokenizer tokenizer =
                new Tokenizer("select[Age>-30](R)");

        List<Token> tokens = tokenizer.tokenize();

        assertTrue(tokens.stream().anyMatch(
                token -> token.getValue().equals(">")
        ));

        assertTrue(tokens.stream().anyMatch(
                token -> token.getValue().equals("-30")
        ));

        assertFalse(tokens.stream().anyMatch(
                token -> token.getValue().equals(">-")
        ));

        assertDoesNotThrow(() ->
                parse("select[Age>-30](R)"));
    }

    // CASE 5
    // ) inside a quoted string must remain part of the string
    @Test
    public void testCase5ParenthesisInsideString() {
        Tokenizer tokenizer =
                new Tokenizer("select[Name='Bob)'](R)");

        List<Token> tokens = tokenizer.tokenize();

        assertTrue(tokens.stream().anyMatch(
                token ->
                        token.getType().equals("STRING")
                                && token.getValue().equals("Bob)")
        ));

        assertDoesNotThrow(() ->
                parse("select[Name='Bob)'](R)"));
    }

    // CASE 6
    // Comma inside a quoted string must remain part of the string
    @Test
    public void testCase6CommaInsideString() {
        Tokenizer tokenizer =
                new Tokenizer("select[Name='a,b'](R)");

        List<Token> tokens = tokenizer.tokenize();

        assertTrue(tokens.stream().anyMatch(
                token ->
                        token.getType().equals("STRING")
                                && token.getValue().equals("a,b")
        ));

        assertDoesNotThrow(() ->
                parse("select[Name='a,b'](R)"));
    }

    // CASE 7
    // Two consecutive quotes inside a string represent one quote
    @Test
    public void testCase7DoubledQuote() {
        Tokenizer tokenizer =
                new Tokenizer("select[Name='O''Brien'](R)");

        List<Token> tokens = tokenizer.tokenize();

        assertTrue(tokens.stream().anyMatch(
                token ->
                        token.getType().equals("STRING")
                                && token.getValue().equals("O'Brien")
        ));

        assertDoesNotThrow(() ->
                parse("select[Name='O''Brien'](R)"));
    }

    // CASE 8
    // A keyword-like word such as "union" can be an attribute
    @Test
    public void testCase8KeywordAsAttribute() {
        Tokenizer tokenizer =
                new Tokenizer("select[union=3](R)");

        List<Token> tokens = tokenizer.tokenize();

        assertTrue(tokens.stream().anyMatch(
                token ->
                        token.getType().equals("IDENTIFIER")
                                && token.getValue().equals("union")
        ));

        assertDoesNotThrow(() ->
                parse("select[union=3](R)"));
    }

    // CASE 9
    // Unterminated string must produce a lexical error with a position
    @Test
    public void testCase9UnterminatedString() {
        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> parse("select[Name='Bob](R)")
                );

        String message =
                error.getMessage().toLowerCase();

        assertTrue(
                message.contains("unterminated"),
                "Error should say the string is unterminated"
        );

        assertTrue(
                message.contains("position"),
                "Error should include the input position"
        );
    }

    // CASE 10
// union and minus are left-associative:
// A union B minus C = (A union B) minus C
    @Test
    public void testCase10UnionMinusAssociativity() {

        String tree =
                treeToString(parse("A union B minus C"));

        int minusPosition = tree.indexOf("Minus");
        int unionPosition = tree.indexOf("Union");

        assertTrue(minusPosition != -1);
        assertTrue(unionPosition != -1);

        // Minus is the root and Union is underneath it
        assertTrue(minusPosition < unionPosition);
    }


    // CASE 11
// minus is left-associative:
// A minus B minus C = (A minus B) minus C
    @Test
    public void testCase11MinusAssociativity() {

        String tree =
                treeToString(parse("A minus B minus C"));

        String expected =
                "Minus\n" +
                        "    Minus\n" +
                        "        Relation(A)\n" +
                        "        Relation(B)\n" +
                        "    Relation(C)\n";

        assertEquals(
                expected.replace("\r\n", "\n"),
                tree.replace("\r\n", "\n")
        );
    }


    // CASE 12
// not binds tighter than and, and binds tighter than or
    @Test
    public void testCase12ConditionPrecedence() {

        String tree = treeToString(
                parse("select[not (a=1 and b=2) or c>3](R)")
        );

        assertTrue(tree.contains("Or("));
        assertTrue(tree.contains("Not("));
        assertTrue(tree.contains("And("));
        assertTrue(tree.contains("Gt("));

        // The outermost condition should be Or
        assertTrue(tree.contains("Select(cond=Or("));
    }


    // CASE 13
// a=1 and b=2 or c=3 groups as:
// (a=1 and b=2) or c=3
    @Test
    public void testCase13AndBeforeOr() {

        String tree = treeToString(
                parse("select[a=1 and b=2 or c=3](R)")
        );

        assertTrue(tree.contains("Select(cond=Or("));
        assertTrue(tree.contains("And("));
    }


    // CASE 14
// Three nested operators must appear in the correct order
    @Test
    public void testCase14NestedExpressions() {

        String tree = treeToString(
                parse(
                        "project[Name](select[Age>30]" +
                                "(select[DID='D1'](Employees)))"
                )
        );

        int projectPosition = tree.indexOf("Project");
        int ageSelectPosition =
                tree.indexOf("Select(cond=Gt");
        int didSelectPosition =
                tree.indexOf("Select(cond=Eq");
        int employeesPosition =
                tree.indexOf("Relation(Employees)");

        assertTrue(projectPosition < ageSelectPosition);
        assertTrue(ageSelectPosition < didSelectPosition);
        assertTrue(didSelectPosition < employeesPosition);
    }


    // CASE 15
// Explicit parentheses override normal grouping
    @Test
    public void testCase15ExplicitParentheses() {

        String tree = treeToString(
                parse("(A union B) minus (C intersect D)")
        );

        String expected =
                "Minus\n" +
                        "    Union\n" +
                        "        Relation(A)\n" +
                        "        Relation(B)\n" +
                        "    Intersect\n" +
                        "        Relation(C)\n" +
                        "        Relation(D)\n";

        assertEquals(
                expected.replace("\r\n", "\n"),
                tree.replace("\r\n", "\n")
        );
    }


    // CASE 16
// Missing closing parenthesis must give a syntax error with position
    @Test
    public void testCase16MissingParenthesis() {

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> parse("select[Age>30](R")
                );

        String message =
                error.getMessage().toLowerCase();

        assertTrue(
                message.contains("position"),
                "Error should include the input position"
        );

        assertTrue(
                message.contains(")")
                        || message.contains("parenthesis"),
                "Error should identify the missing parenthesis"
        );
    }


    // CASE 17
// Projection cannot have an empty attribute list
    @Test
    public void testCase17EmptyProjection() {

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> parse("project[](R)")
                );

        assertNotNull(error.getMessage());
        assertFalse(error.getMessage().isEmpty());
    }
}