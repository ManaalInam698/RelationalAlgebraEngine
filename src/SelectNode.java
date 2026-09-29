public class SelectNode extends ParseNode {

    private ConditionNode condition;
    private ParseNode child;

    public SelectNode(ConditionNode condition, ParseNode child) {
        this.condition = condition;
        this.child = child;
    }

    @Override
    public void printTree(String indent) {
        System.out.println(indent + "Select(cond=" + condition + ")");
        child.printTree(indent + "    ");
    }

    public ConditionNode getCondition() {
        return condition;
    }

    public ParseNode getChild() {
        return child;
    }
}