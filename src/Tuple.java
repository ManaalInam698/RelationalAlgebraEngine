import java.util.ArrayList;
import java.util.List;

public class Tuple {

    private List<Value> values;

    public Tuple(List<Value> values) {
        this.values = new ArrayList<>(values);
    }

    public List<Value> getValues() {
        return values;
    }

    public Value getValue(int index) {
        return values.get(index);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Tuple)) {
            return false;
        }

        Tuple other = (Tuple) obj;

        if (values.size() != other.values.size()) {
            return false;
        }

        for (int i = 0; i < values.size(); i++) {
            if (!values.get(i).equals(other.values.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;

        for (Value value : values) {
            result = 31 * result + value.hashCode();
        }

        return result;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}