import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectProjectExperiment {

    public static void main(String[] args) {

        int[] sizes = {1000, 2000, 4000, 8000, 16000, 32000, 64000};

        System.out.println("n\tselect examined\tselect time (s)\tproject time (s)");

        for (int size : sizes) {

            Database database = new Database();
            database.addRelation(createR(size));
            Evaluator evaluator = new Evaluator(database);

            ParseNode selectTree = parse("select[b<100](R)");
            ParseNode projectTree = parse("project[b](R)");

            // Warm-up: run each once without timing it
            evaluator.evaluate(selectTree);
            evaluator.evaluate(projectTree);

            // Time select
            RelationalAlgebra.resetCounters();
            long start = System.nanoTime();
            evaluator.evaluate(selectTree);
            double selectTime = (System.nanoTime() - start) / 1_000_000_000.0;
            long examined = RelationalAlgebra.getSelectExaminationCount();

            // Time project
            start = System.nanoTime();
            evaluator.evaluate(projectTree);
            double projectTime = (System.nanoTime() - start) / 1_000_000_000.0;

            System.out.println(size + "\t" + examined + "\t"
                    + selectTime + "\t" + projectTime);
        }
    }

    private static ParseNode parse(String query) {
        Tokenizer tokenizer = new Tokenizer(query);
        Parser parser = new Parser(tokenizer.tokenize());
        return parser.parse();
    }

    // Same data as PerformanceExperiment: R(a, b) with a = b = i
    private static Relation createR(int size) {
        Relation r = new Relation("R", Arrays.asList("a", "b"));
        for (int i = 0; i < size; i++) {
            List<Value> values = new ArrayList<>();
            values.add(new Value(Integer.toString(i), true));
            values.add(new Value(Integer.toString(i), true));
            r.addTuple(new Tuple(values));
        }
        return r;
    }
}