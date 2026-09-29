import java.util.List;

public class ProjectNode extends ParseNode {

    private List<String> attributes;
    private ParseNode child;

    public ProjectNode(List<String> attributes, ParseNode child) {
        this.attributes = attributes;
        this.child = child;
    }

    @Override
    public void printTree(String indent) {
        System.out.println(indent + "Project(attrs=" + attributes + ")");
        child.printTree(indent + "    ");
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public ParseNode getChild() {
        return child;
    }
}