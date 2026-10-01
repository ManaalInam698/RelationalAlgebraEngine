import java.util.ArrayList;
import java.util.List;

public class Parser {

    private List<Token> tokens;
    private int position;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
    }

    public ParseNode parse() {
        ParseNode tree = parseExpression();

        if (position < tokens.size()) {
            throw new IllegalArgumentException(
                    "Unexpected token '" + current().getValue()
                            + "' at position " + current().getPosition());
        }

        return tree;
    }

    private ParseNode parseExpression() {
        return parseSetExpression();
    }

    private ParseNode parseSetExpression() {

        ParseNode left = parseProductExpression();

        while (check("union") || check("intersect") || check("minus")) {

            String operator = current().getValue();
            position++;

            ParseNode right = parseProductExpression();

            left = new BinaryNode(
                    capitalize(operator), left, right);
        }

        return left;
    }

    private ParseNode parseProductExpression() {

        ParseNode left = parseUnaryExpression();

        while (check("times") || check("join")) {

            String operator = current().getValue();
            position++;

            if (operator.equals("join")) {

                expect("[");
                ConditionNode condition = parseCondition();                expect("]");

                ParseNode right = parseUnaryExpression();

                left = new BinaryNode(
                        "Join", left, right, condition);

            } else {

                ParseNode right = parseUnaryExpression();

                left = new BinaryNode("Times", left, right);
            }
        }

        return left;
    }

    private ParseNode parseUnaryExpression() {

        if (check("select")) {
            return parseSelect();
        }

        if (check("project")) {
            return parseProject();
        }

        if (check("rename")) {
            return parseRename();
        }

        if (check("(")) {
            position++;
            ParseNode node = parseExpression();
            expect(")");
            return node;
        }

        Token relation = current();
        position++;

        return new RelationNode(relation.getValue());
    }

    private ParseNode parseSelect() {

        expect("select");
        expect("[");

        ConditionNode condition = parseCondition();
        expect("]");
        expect("(");

        ParseNode child = parseExpression();

        expect(")");

        return new SelectNode(condition, child);
    }

    private ParseNode parseProject() {

        expect("project");
        expect("[");

        List<String> attributes = new ArrayList<>();

        attributes.add(parseAttribute());

        while (check(",")) {
            position++;
            attributes.add(parseAttribute());
        }

        expect("]");
        expect("(");

        ParseNode child = parseExpression();

        expect(")");

        return new ProjectNode(attributes, child);
    }

    private ParseNode parseRename() {

        expect("rename");
        expect("[");

        String newName = current().getValue();
        position++;

        expect("]");
        expect("(");

        ParseNode child = parseExpression();

        expect(")");

        return new RenameNode(newName, child);
    }

    private String parseAttribute() {

        String attribute = current().getValue();
        position++;

        if (check(".")) {
            position++;
            attribute += "." + current().getValue();
            position++;
        }

        return attribute;
    }

    private ConditionNode parseCondition() {
        return parseOrCondition();
    }

    private ConditionNode parseOrCondition() {
        ConditionNode left = parseAndCondition();

        while (check("or")) {
            position++;
            ConditionNode right = parseAndCondition();
            left = new LogicalNode("or", left, right);
        }

        return left;
    }

    private ConditionNode parseAndCondition() {
        ConditionNode left = parseNotCondition();

        while (check("and")) {
            position++;
            ConditionNode right = parseNotCondition();
            left = new LogicalNode("and", left, right);
        }

        return left;
    }

    private ConditionNode parseNotCondition() {
        if (check("not")) {
            position++;
            return new NotNode(parseNotCondition());
        }

        return parseConditionPrimary();
    }

    private ConditionNode parseConditionPrimary() {
        if (check("(")) {
            position++;
            ConditionNode condition = parseCondition();
            expect(")");
            return condition;
        }

        return parseComparison();
    }

    private ConditionNode parseComparison() {
        OperandNode left = parseOperand();

        if (position >= tokens.size()) {
            throw new IllegalArgumentException(
                    "Missing comparison operator at position "
                            + getEndPosition());
        }

        String operator = current().getValue();

        if (!operator.equals("=")
                && !operator.equals("!=")
                && !operator.equals("<")
                && !operator.equals("<=")
                && !operator.equals(">")
                && !operator.equals(">=")) {

            throw new IllegalArgumentException(
                    "Expected comparison operator at position "
                            + current().getPosition());
        }

        position++;

        if (position >= tokens.size() || check("]") || check(")")) {
            int errorPosition = position >= tokens.size()
                    ? getEndPosition()
                    : current().getPosition();

            throw new IllegalArgumentException(
                    "Missing operand at position " + errorPosition);
        }

        OperandNode right = parseOperand();

        return new ComparisonNode(left, operator, right);
    }

    private OperandNode parseOperand() {
        Token token = current();

        if (token.getType().equals("NUMBER")) {
            position++;
            return new OperandNode("Num", token.getValue());
        }

        if (token.getType().equals("STRING")) {
            position++;
            return new OperandNode("Str", token.getValue());
        }

        String attribute = parseAttribute();
        return new OperandNode("Attr", attribute);
    }

    private boolean check(String value) {
        return position < tokens.size()
                && current().getValue().equals(value);
    }

    private void expect(String value) {

        if (!check(value)) {

            if (position >= tokens.size()) {
                throw new IllegalArgumentException(
                        "Expected '" + value + "' at position "
                                + getEndPosition());
            }

            throw new IllegalArgumentException(
                    "Expected '" + value + "' at position "
                            + current().getPosition());
        }

        position++;
    }

    private Token current() {
        if (position >= tokens.size()) {
            throw new IllegalArgumentException(
                    "Unexpected end of input at position "
                            + getEndPosition());
        }

        return tokens.get(position);
    }

    private int getEndPosition() {
        if (tokens.isEmpty()) {
            return 0;
        }

        Token last = tokens.get(tokens.size() - 1);
        return last.getPosition() + last.getValue().length();
    }

    private String capitalize(String word) {
        return Character.toUpperCase(word.charAt(0))
                + word.substring(1);
    }


    public static void main(String[] args) {

        String query =
                "select[Age>30 or DID=2 and not Name='Bob'](Employees)";
        Tokenizer tokenizer = new Tokenizer(query);

        List<Token> tokens = tokenizer.tokenize();

        Parser parser = new Parser(tokens);
        ParseNode tree = parser.parse();

        tree.printTree();
    }
}