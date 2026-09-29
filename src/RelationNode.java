public class RelationNode extends ParseNode {

    private String name;

    public RelationNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void printTree(String indent) {
        System.out.println(indent + "Relation(" + name + ")");
    }
}