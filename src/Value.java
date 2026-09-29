public class Value {

    private String value;
    private boolean number;

    public Value(String value, boolean number) {
        this.value = value;
        this.number = number;
    }

    public String getValue() {
        return value;
    }

    public boolean isNumber() {
        return number;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Value)) {
            return false;
        }

        Value other = (Value) obj;

        return value.equals(other.value)
                && number == other.number;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        return 31 * result + (number ? 1 : 0);
    }

    @Override
    public String toString() {
        return value;
    }
}