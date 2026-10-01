import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PerformanceExperiment {

    public static void main(String[] args) {

        int[] sizes = {
                1000,
                2000,
                4000,
                8000,
                16000,
                32000,
                64000
        };

        System.out.println(
                "n\tm\tcomparisons\twall time (s)\toutput tuples"
        );

        for (int size : sizes) {

            Relation r = createR(size);
            Relation s = createS(size);

            Database database = new Database();
            database.addRelation(r);
            database.addRelation(s);

            Tokenizer tokenizer =
                    new Tokenizer("R join[R.b=S.b] S");

            Parser parser =
                    new Parser(tokenizer.tokenize());

            ParseNode tree = parser.parse();

            Evaluator evaluator = new Evaluator(database);

            RelationalAlgebra.resetCounters();

            long startTime = System.nanoTime();

            Relation result = evaluator.evaluate(tree);

            long endTime = System.nanoTime();

            double seconds =
                    (endTime - startTime) / 1_000_000_000.0;

            long comparisons =
                    RelationalAlgebra.getJoinComparisonCount();

            int outputTuples =
                    result.getTuples().size();

            System.out.println(
                    size + "\t"
                            + size + "\t"
                            + comparisons + "\t"
                            + seconds + "\t"
                            + outputTuples
            );
        }
    }

    private static Relation createR(int size) {

        Relation r = new Relation(
                "R",
                Arrays.asList("a", "b")
        );

        for (int i = 0; i < size; i++) {

            List<Value> values = new ArrayList<>();

            values.add(new Value(
                    Integer.toString(i), true));

            values.add(new Value(
                    Integer.toString(i), true));

            r.addTuple(new Tuple(values));
        }

        return r;
    }

    private static Relation createS(int size) {

        Relation s = new Relation(
                "S",
                Arrays.asList("b", "c")
        );

        for (int i = 0; i < size; i++) {

            List<Value> values = new ArrayList<>();

            values.add(new Value(
                    Integer.toString(i), true));

            values.add(new Value(
                    Integer.toString(i), true));

            s.addTuple(new Tuple(values));
        }

        return s;
    }
}