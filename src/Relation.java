import java.util.ArrayList;
import java.util.List;

public class Relation {

    private String name;
    private List<String> attributes;
    private List<Tuple> tuples;

    public Relation(String name, List<String> attributes) {
        this.name = name;
        this.attributes = new ArrayList<>(attributes);
        this.tuples = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public List<Tuple> getTuples() {
        return tuples;
    }

    public void addTuple(Tuple tuple) {
        if (!tuples.contains(tuple)) {
            tuples.add(tuple);
        }
    }

    public int getAttributeIndex(String attribute) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).equals(attribute)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(name)
                .append(" ")
                .append(attributes)
                .append("\n");

        for (Tuple tuple : tuples) {
            result.append(tuple).append("\n");
        }

        return result.toString();
    }
}