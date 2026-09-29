import java.util.ArrayList;
import java.util.List;

public class Database {

    private List<Relation> relations;

    public Database() {
        relations = new ArrayList<>();
    }

    public void addRelation(Relation relation) {
        relations.add(relation);
    }

    public Relation getRelation(String name) {

        for (Relation relation : relations) {
            if (relation.getName().equals(name)) {
                return relation;
            }
        }

        throw new IllegalArgumentException(
                "Name error: unknown relation '" + name + "'");
    }
}