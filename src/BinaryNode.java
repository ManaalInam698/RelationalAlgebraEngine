public class BinaryNode extends ParseNode {

    private String operator;
    private ParseNode left;
    private ParseNode right;
    private ConditionNode condition;

    public BinaryNode(String operator, ParseNode left, ParseNode right) {
        this(operator, left, right, null);
    }

    public BinaryNode(String operator, ParseNode left,
                      ParseNode right, ConditionNode condition) {
        this.operator = operator;
        this.left = left;
        this.right = right;
        this.condition = condition;
    }

    @Override
    public void printTree(String indent) {

        if (condition != null) {
            System.out.println(indent + operator
                    + "(cond=" + condition + ")");
        } else {
            System.out.println(indent + operator);
        }

        left.printTree(indent + "    ");
        right.printTree(indent + "    ");
    }

    public String getOperator() {
        return operator;
    }

    public ParseNode getLeft() {
        return left;
    }

    public ParseNode getRight() {
        return right;
    }

    public ConditionNode getCondition() {
        return condition;
    }
}