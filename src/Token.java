public class Token {

    private String type;
    private String value;
    private int position;

    public Token(String type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return type + "(" + value + ") at position " + position;
    }
}
