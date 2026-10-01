import java.util.ArrayList;
import java.util.List;

public class RelationalAlgebra {

    private static long joinComparisonCount = 0;
    private static long selectExaminationCount = 0;

    public static Relation rename(Relation relation, String newName) {

        Relation result = new Relation(
                newName,
                relation.getAttributes()
        );

        for (Tuple tuple : relation.getTuples()) {
            result.addTuple(tuple);
        }

        return result;
    }

    public static Relation project(Relation relation, List<String> attributes) {

        for (int i = 0; i < attributes.size(); i++) {
            for (int j = i + 1; j < attributes.size(); j++) {
                if (attributes.get(i).equals(attributes.get(j))) {
                    throw new IllegalArgumentException(
                            "Schema error: duplicate projection attribute '"
                                    + attributes.get(i) + "'");
                }
            }
        }
        Relation result = new Relation(
                relation.getName(),
                attributes
        );

        for (Tuple tuple : relation.getTuples()) {

            List<Value> newValues = new ArrayList<>();
            for (String attribute : attributes) {
                int index = relation.getAttributeIndex(attribute);

                if (index == -1) {
                    throw new IllegalArgumentException(
                            "Name error: unknown attribute '" + attribute + "'");
                }

                newValues.add(tuple.getValue(index));
            }

            result.addTuple(new Tuple(newValues));
        }

        return result;
    }

    public static Relation union(Relation left, Relation right) {
        checkCompatible(left, right);

        Relation result = new Relation(
                left.getName(),
                left.getAttributes()
        );

        for (Tuple tuple : left.getTuples()) {
            result.addTuple(tuple);
        }

        for (Tuple tuple : right.getTuples()) {
            result.addTuple(tuple);
        }

        return result;
    }

    public static Relation intersect(Relation left, Relation right) {
        checkCompatible(left, right);

        Relation result = new Relation(
                left.getName(),
                left.getAttributes()
        );

        for (Tuple tuple : left.getTuples()) {
            if (right.getTuples().contains(tuple)) {
                result.addTuple(tuple);
            }
        }

        return result;
    }

    public static Relation minus(Relation left, Relation right) {
        checkCompatible(left, right);

        Relation result = new Relation(
                left.getName(),
                left.getAttributes()
        );

        for (Tuple tuple : left.getTuples()) {
            if (!right.getTuples().contains(tuple)) {
                result.addTuple(tuple);
            }
        }

        return result;
    }

    private static void checkCompatible(Relation left, Relation right) {
        if (!left.getAttributes().equals(right.getAttributes())) {
            throw new IllegalArgumentException(
                    "Schema error: relations are not union compatible");
        }
    }


    public static Relation times(Relation left, Relation right) {

        List<String> newAttributes = new ArrayList<>();

        for (String attribute : left.getAttributes()) {
            newAttributes.add(left.getName() + "." + attribute);
        }

        for (String attribute : right.getAttributes()) {
            newAttributes.add(right.getName() + "." + attribute);
        }

        Relation result = new Relation(
                left.getName() + "_times_" + right.getName(),
                newAttributes
        );

        for (Tuple leftTuple : left.getTuples()) {
            for (Tuple rightTuple : right.getTuples()) {

                List<Value> newValues = new ArrayList<>();
                newValues.addAll(leftTuple.getValues());
                newValues.addAll(rightTuple.getValues());

                result.addTuple(new Tuple(newValues));
            }
        }

        return result;
    }

    public static Relation select(Relation relation, ConditionNode condition) {

        Relation result = new Relation(
                relation.getName(),
                relation.getAttributes()
        );

        for (Tuple tuple : relation.getTuples()) {

            selectExaminationCount++;

            if (evaluateCondition(condition, relation, tuple)) {
                result.addTuple(tuple);
            }
        }

        return result;
    }


    private static boolean evaluateCondition(
            ConditionNode condition,
            Relation relation,
            Tuple tuple) {

        if (condition instanceof LogicalNode) {
            LogicalNode logical = (LogicalNode) condition;

            boolean left = evaluateCondition(
                    logical.getLeft(), relation, tuple);

            boolean right = evaluateCondition(
                    logical.getRight(), relation, tuple);

            if (logical.getOperator().equals("and")) {
                return left && right;
            }

            return left || right;
        }

        if (condition instanceof NotNode) {
            NotNode not = (NotNode) condition;

            return !evaluateCondition(
                    not.getCondition(), relation, tuple);
        }

        if (condition instanceof ComparisonNode) {
            return evaluateComparison(
                    (ComparisonNode) condition,
                    relation,
                    tuple);
        }

        throw new IllegalArgumentException(
                "Unknown condition type");
    }

    private static boolean evaluateComparison(
            ComparisonNode comparison,
            Relation relation,
            Tuple tuple) {

        OperandNode left = comparison.getLeft();
        OperandNode right = comparison.getRight();

        Value leftValue = getOperandValue(left, relation, tuple);
        Value rightValue = getOperandValue(right, relation, tuple);

        boolean leftNumber = leftValue.isNumber();
        boolean rightNumber = rightValue.isNumber();

        if (leftNumber != rightNumber) {
            throw new IllegalArgumentException(
                    "Type error: cannot compare a number to a string");
        }

        int comparisonResult;

        if (leftNumber) {
            double leftNum =
                    Double.parseDouble(leftValue.getValue());
            double rightNum =
                    Double.parseDouble(rightValue.getValue());

            comparisonResult =
                    Double.compare(leftNum, rightNum);

        } else {
            comparisonResult =
                    leftValue.getValue().compareTo(
                            rightValue.getValue());
        }

        switch (comparison.getOperator()) {
            case "=":
                return comparisonResult == 0;
            case "!=":
                return comparisonResult != 0;
            case "<":
                return comparisonResult < 0;
            case "<=":
                return comparisonResult <= 0;
            case ">":
                return comparisonResult > 0;
            case ">=":
                return comparisonResult >= 0;
            default:
                throw new IllegalArgumentException(
                        "Unknown comparison operator '"
                                + comparison.getOperator() + "'");
        }
    }

    private static Value getOperandValue(
            OperandNode operand,
            Relation relation,
            Tuple tuple) {

        if (operand.getType().equals("Num")) {
            return new Value(operand.getValue(), true);
        }

        if (operand.getType().equals("Str")) {
            return new Value(operand.getValue(), false);
        }

        int index =
                relation.getAttributeIndex(operand.getValue());

        if (index == -1) {
            throw new IllegalArgumentException(
                    "Name error: unknown attribute '"
                            + operand.getValue() + "'");
        }

        return tuple.getValue(index);
    }

    public static Relation join(
            Relation left,
            Relation right,
            ConditionNode condition) {

        List<String> newAttributes = new ArrayList<>();

        for (String attribute : left.getAttributes()) {
            newAttributes.add(left.getName() + "." + attribute);
        }

        for (String attribute : right.getAttributes()) {
            newAttributes.add(right.getName() + "." + attribute);
        }

        Relation result = new Relation(
                left.getName() + "_join_" + right.getName(),
                newAttributes
        );

        Relation combinedRelation = new Relation(
                left.getName() + "_times_" + right.getName(),
                newAttributes
        );

        for (Tuple leftTuple : left.getTuples()) {

            for (Tuple rightTuple : right.getTuples()) {

                joinComparisonCount++;

                List<Value> combinedValues = new ArrayList<>();
                combinedValues.addAll(leftTuple.getValues());
                combinedValues.addAll(rightTuple.getValues());

                Tuple combinedTuple = new Tuple(combinedValues);

                if (evaluateCondition(
                        condition,
                        combinedRelation,
                        combinedTuple)) {

                    result.addTuple(combinedTuple);
                }
            }
        }

        return result;
    }


            public static void resetCounters() {
                joinComparisonCount = 0;
                selectExaminationCount = 0;
            }

            public static long getJoinComparisonCount() {
                return joinComparisonCount;
            }

            public static long getSelectExaminationCount() {
                return selectExaminationCount;
            }

}