public class LogicalNode extends ConditionNode {

    private String operator;
    private ConditionNode left;
    private ConditionNode right;

    public LogicalNode(String operator, ConditionNode left, ConditionNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        String name;

        if (operator.equals("and")) {
            name = "And";
        } else {
            name = "Or";
        }

        return name + "(" + left + ", " + right + ")";
    }

    public String getOperator() {
        return operator;
    }

    public ConditionNode getLeft() {
        return left;
    }

    public ConditionNode getRight() {
        return right;
    }
}