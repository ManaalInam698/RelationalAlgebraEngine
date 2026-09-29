public class OperandNode {

    private String type;
    private String value;

    public OperandNode(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + "(" + value + ")";
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}