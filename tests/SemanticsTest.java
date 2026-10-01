import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SemanticsTest {

    private Relation evaluate(Database database, String query) {
        Tokenizer tokenizer = new Tokenizer(query);
        Parser parser = new Parser(tokenizer.tokenize());
        ParseNode tree = parser.parse();

        Evaluator evaluator = new Evaluator(database);
        return evaluator.evaluate(tree);
    }


    // CASE 18
    // A and B are both attributes, so their column values are compared.
    @Test
    public void testCase18AttributeToAttributeComparison() {

        Database database = new Database();

        Relation r = new Relation(
                "R",
                Arrays.asList("A", "B")
        );

        r.addTuple(new Tuple(Arrays.asList(
                new Value("5", true),
                new Value("5", true)
        )));

        r.addTuple(new Tuple(Arrays.asList(
                new Value("5", true),
                new Value("7", true)
        )));

        database.addRelation(r);

        Relation result =
                evaluate(database, "select[A=B](R)");

        assertEquals(1, result.getTuples().size());

        assertEquals(
                "5",
                result.getTuples().get(0).getValue(0).getValue()
        );

        assertEquals(
                "5",
                result.getTuples().get(0).getValue(1).getValue()
        );
    }


    // CASE 19
    // Qualified attributes must work during a join.
    // Both DID columns must remain distinguishable.
    @Test
    public void testCase19QualifiedJoin() {

        Database database = new Database();

        Relation emp = new Relation(
                "Emp",
                Arrays.asList("EID", "DID")
        );

        emp.addTuple(new Tuple(Arrays.asList(
                new Value("E1", false),
                new Value("D1", false)
        )));

        emp.addTuple(new Tuple(Arrays.asList(
                new Value("E2", false),
                new Value("D2", false)
        )));

        Relation dept = new Relation(
                "Dept",
                Arrays.asList("DID", "Name")
        );

        dept.addTuple(new Tuple(Arrays.asList(
                new Value("D1", false),
                new Value("Sales", false)
        )));

        dept.addTuple(new Tuple(Arrays.asList(
                new Value("D2", false),
                new Value("IT", false)
        )));

        database.addRelation(emp);
        database.addRelation(dept);

        Relation result = evaluate(
                database,
                "Emp join[Emp.DID=Dept.DID] Dept"
        );

        assertEquals(2, result.getTuples().size());

        assertTrue(result.getAttributes().contains("Emp.DID"));
        assertTrue(result.getAttributes().contains("Dept.DID"));

        assertNotEquals(
                result.getAttributeIndex("Emp.DID"),
                result.getAttributeIndex("Dept.DID")
        );
    }


    // CASE 20
    // Rename allows the two copies of Emp to have different qualified names.
    @Test
    public void testCase20SelfJoinWithRename() {

        Database database = new Database();

        Relation emp = new Relation(
                "Emp",
                Arrays.asList("EID", "MgrID")
        );

        emp.addTuple(new Tuple(Arrays.asList(
                new Value("E1", false),
                new Value("E2", false)
        )));

        emp.addTuple(new Tuple(Arrays.asList(
                new Value("E2", false),
                new Value("E3", false)
        )));

        emp.addTuple(new Tuple(Arrays.asList(
                new Value("E3", false),
                new Value("NONE", false)
        )));

        database.addRelation(emp);

        Relation result = evaluate(
                database,
                "rename[E2](Emp) join[Emp.MgrID=E2.EID] Emp"
        );

        assertEquals(2, result.getTuples().size());

        assertTrue(result.getAttributes().contains("E2.EID"));
        assertTrue(result.getAttributes().contains("Emp.MgrID"));
    }


    // CASE 21
    // Union with different schemas must produce a schema error.
    @Test
    public void testCase21IncompatibleUnion() {

        Database database = new Database();

        Relation r = new Relation(
                "R",
                Arrays.asList("A", "B")
        );

        Relation s = new Relation(
                "S",
                Arrays.asList("A", "C")
        );

        database.addRelation(r);
        database.addRelation(s);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> evaluate(database, "R union S")
                );

        assertTrue(
                error.getMessage().toLowerCase().contains("schema")
        );
    }


    // CASE 22
    // Age is numeric but '30' is a string.
    @Test
    public void testCase22NumberStringTypeError() {

        Database database = new Database();

        Relation r = new Relation(
                "R",
                Arrays.asList("Age")
        );

        r.addTuple(new Tuple(Arrays.asList(
                new Value("32", true)
        )));

        database.addRelation(r);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> evaluate(
                                database,
                                "select[Age>'30'](R)"
                        )
                );

        assertTrue(
                error.getMessage().toLowerCase().contains("type")
        );
    }


    // CASE 23
    // Projection must remove duplicate tuples.
    @Test
    public void testCase23ProjectionRemovesDuplicates() {

        Database database = new Database();

        Relation employees = new Relation(
                "Employees",
                Arrays.asList("EID", "Name", "Age", "DID")
        );

        employees.addTuple(new Tuple(Arrays.asList(
                new Value("E1", false),
                new Value("John", false),
                new Value("32", true),
                new Value("D1", false)
        )));

        employees.addTuple(new Tuple(Arrays.asList(
                new Value("E2", false),
                new Value("Alice", false),
                new Value("28", true),
                new Value("D2", false)
        )));

        employees.addTuple(new Tuple(Arrays.asList(
                new Value("E3", false),
                new Value("Bob", false),
                new Value("29", true),
                new Value("D1", false)
        )));

        database.addRelation(employees);

        Relation result =
                evaluate(database, "project[DID](Employees)");

        assertEquals(2, result.getTuples().size());

        assertEquals(
                Arrays.asList("DID"),
                result.getAttributes()
        );
    }


    // CASE 24
    // We choose to reject duplicate projection attributes.
    @Test
    public void testCase24DuplicateProjectionAttribute() {

        Database database = new Database();

        Relation r = new Relation(
                "R",
                Arrays.asList("Name")
        );

        r.addTuple(new Tuple(Arrays.asList(
                new Value("Bob", false)
        )));

        database.addRelation(r);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> evaluate(
                                database,
                                "project[Name, Name](R)"
                        )
                );

        assertTrue(
                error.getMessage().toLowerCase().contains("duplicate")
        );
    }


    // CASE 25
    // An empty result still prints its schema cleanly.
    @Test
    public void testCase25EmptyResultPrintsSchema() {

        Database database = new Database();

        Relation r = new Relation(
                "R",
                Arrays.asList("Age")
        );

        r.addTuple(new Tuple(Arrays.asList(
                new Value("20", true)
        )));

        database.addRelation(r);

        Relation result =
                evaluate(database, "select[Age>100](R)");

        assertEquals(0, result.getTuples().size());

        String output = result.toString();

        assertTrue(output.contains("R"));
        assertTrue(output.contains("Age"));
        assertEquals("R [Age]\n", output);
    }

    @Test
    public void testExtraRename() {

        Database database = new Database();

        Relation r = new Relation("R", Arrays.asList("Name"));

        database.addRelation(r);

        Relation result = evaluate(database, "rename[S](R)");

        assertEquals("S", result.getName());
    }
}