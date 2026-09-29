public abstract class ParseNode {

    public abstract void printTree(String indent);

    public void printTree() {
        printTree("");
    }
}