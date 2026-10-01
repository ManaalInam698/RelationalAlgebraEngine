import java.io.FileWriter;
import java.io.IOException;

public class DataGenerator {

    public static void generate(
            int rSize,
            int sSize,
            int matchRate) throws IOException {

        FileWriter rWriter = new FileWriter("R.txt");
        FileWriter sWriter = new FileWriter("S.txt");

        // Generate R(a, b)
        rWriter.write("R(a, b) = {\n");

        for (int i = 0; i < rSize; i++) {
            int a = i;
            int b = i;

            rWriter.write(a + ", " + b + "\n");
        }

        rWriter.write("}\n");
        rWriter.close();


        // Generate S(b, c)
        sWriter.write("S(b, c) = {\n");

        for (int i = 0; i < sSize; i++) {

            // Repeating b values creates multiple matches
            // for tuples from R.
            int b = i / matchRate;
            int c = i;

            sWriter.write(b + ", " + c + "\n");
        }

        sWriter.write("}\n");
        sWriter.close();
    }


    public static void main(String[] args) {

        try {
            generate(1000, 1000, 1);

            System.out.println("R.txt and S.txt generated.");

        } catch (IOException e) {
            System.out.println(
                    "Error creating relation files: "
                            + e.getMessage()
            );
        }
    }
}