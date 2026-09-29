public class NotNode extends ConditionNode {

    private ConditionNode condition;

    public NotNode(ConditionNode condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "Not(" + condition + ")";
    }
    public ConditionNode getCondition() {
        return condition;
    }
}