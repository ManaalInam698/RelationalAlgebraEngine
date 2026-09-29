public class RenameNode extends ParseNode {

    private String newName;
    private ParseNode child;

    public RenameNode(String newName, ParseNode child) {
        this.newName = newName;
        this.child = child;
    }

    @Override
    public void printTree(String indent) {
        System.out.println(indent + "Rename(name=" + newName + ")");
        child.printTree(indent + "    ");
    }

    public String getNewName() {
        return newName;
    }

    public ParseNode getChild() {
        return child;
    }
}