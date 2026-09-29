import java.util.List;

public class Evaluator {

    private Database database;

    public Evaluator(Database database) {
        this.database = database;
    }

    public Relation evaluate(ParseNode node) {

        if (node instanceof RelationNode) {
            RelationNode relationNode = (RelationNode) node;
            return database.getRelation(relationNode.getName());
        }

        if (node instanceof SelectNode) {
            SelectNode selectNode = (SelectNode) node;

            Relation child = evaluate(selectNode.getChild());

            return RelationalAlgebra.select(
                    child,
                    selectNode.getCondition()
            );
        }

        if (node instanceof ProjectNode) {
            ProjectNode projectNode = (ProjectNode) node;

            Relation child = evaluate(projectNode.getChild());

            return RelationalAlgebra.project(
                    child,
                    projectNode.getAttributes()
            );
        }

        if (node instanceof RenameNode) {
            RenameNode renameNode = (RenameNode) node;

            Relation child = evaluate(renameNode.getChild());

            return RelationalAlgebra.rename(
                    child,
                    renameNode.getNewName()
            );
        }

        if (node instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) node;

            Relation left = evaluate(binaryNode.getLeft());
            Relation right = evaluate(binaryNode.getRight());

            switch (binaryNode.getOperator()) {

                case "Union":
                    return RelationalAlgebra.union(left, right);

                case "Intersect":
                    return RelationalAlgebra.intersect(left, right);

                case "Minus":
                    return RelationalAlgebra.minus(left, right);

                case "Times":
                    return RelationalAlgebra.times(left, right);

                case "Join":
                    return RelationalAlgebra.join(
                            left,
                            right,
                            binaryNode.getCondition()
                    );

                default:
                    throw new IllegalArgumentException(
                            "Unknown relational operator '"
                                    + binaryNode.getOperator() + "'");
            }
        }

        throw new IllegalArgumentException(
                "Unknown parse tree node");
    }
}