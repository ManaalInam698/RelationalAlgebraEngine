public class ComparisonNode extends ConditionNode {

    private OperandNode left;
    private String operator;
    private OperandNode right;

    public ComparisonNode(OperandNode left, String operator, OperandNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString() {
        String name;

        switch (operator) {
            case ">":  name = "Gt"; break;
            case "<":  name = "Lt"; break;
            case ">=": name = "Ge"; break;
            case "<=": name = "Le"; break;
            case "=":  name = "Eq"; break;
            case "!=": name = "Ne"; break;
            default:   name = operator;
        }

        return name + "(" + left + ", " + right + ")";
    }

    public OperandNode getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public OperandNode getRight() {
        return right;
    }
}