import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatchRateExperiment {

    public static void main(String[] args) {

        int n = 4000;
        int[] matchRates = {0, 1, 2, 5, 10};

        System.out.println("match rate\tcomparisons\twall time (s)\toutput tuples");

        for (int rate : matchRates) {

            Database database = new Database();
            database.addRelation(createR(n, rate));
            database.addRelation(createS(n, rate));
            Evaluator evaluator = new Evaluator(database);

            Tokenizer tokenizer = new Tokenizer("R join[R.b=S.b] S");
            ParseNode tree = new Parser(tokenizer.tokenize()).parse();

            RelationalAlgebra.resetCounters();
            long start = System.nanoTime();
            Relation result = evaluator.evaluate(tree);
            double seconds = (System.nanoTime() - start) / 1_000_000_000.0;

            System.out.println(rate + "\t"
                    + RelationalAlgebra.getJoinComparisonCount() + "\t"
                    + seconds + "\t"
                    + result.getTuples().size());
        }
    }

    // R(a, b): b cycles through n / rate values, so each b value
    // appears in exactly `rate` tuples of S
    private static Relation createR(int n, int rate) {
        Relation r = new Relation("R", Arrays.asList("a", "b"));
        for (int i = 0; i < n; i++) {
            int b = (rate == 0) ? i : i % (n / rate);
            r.addTuple(makeTuple(i, b));
        }
        return r;
    }

    // S(b, c): with rate 0, b values start at n so nothing in R matches
    private static Relation createS(int n, int rate) {
        Relation s = new Relation("S", Arrays.asList("b", "c"));
        for (int i = 0; i < n; i++) {
            int b = (rate == 0) ? i + n : i % (n / rate);
            s.addTuple(makeTuple(b, i));
        }
        return s;
    }

    private static Tuple makeTuple(int first, int second) {
        List<Value> values = new ArrayList<>();
        values.add(new Value(Integer.toString(first), true));
        values.add(new Value(Integer.toString(second), true));
        return new Tuple(values);
    }
}