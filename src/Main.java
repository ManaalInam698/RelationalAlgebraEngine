import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        try {
            Database database = new Database();

            Relation employees = new Relation(
                    "Employees",
                    Arrays.asList("EID", "Name", "Age", "DID")
            );

            employees.addTuple(new Tuple(
                    Arrays.asList(
                            new Value("E1", false),
                            new Value("John", false),
                            new Value("32", true),
                            new Value("D1", false)
                    )
            ));

            employees.addTuple(new Tuple(
                    Arrays.asList(
                            new Value("E2", false),
                            new Value("Alice", false),
                            new Value("28", true),
                            new Value("D2", false)
                    )
            ));

            employees.addTuple(new Tuple(
                    Arrays.asList(
                            new Value("E3", false),
                            new Value("Bob", false),
                            new Value("29", true),
                            new Value("D1", false)
                    )
            ));

            database.addRelation(employees);
            Relation departments = new Relation(
                    "Departments",
                    Arrays.asList("DID", "DepartmentName")
            );

            database.addRelation(departments);

            String query = "Employees union Departments";
            Tokenizer tokenizer = new Tokenizer(query);
            Parser parser = new Parser(tokenizer.tokenize());

            ParseNode tree = parser.parse();

            Evaluator evaluator = new Evaluator(database);
            Relation result = evaluator.evaluate(tree);

            System.out.println(result);

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}