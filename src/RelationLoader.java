import java.util.ArrayList;
import java.util.List;

public class RelationLoader {

    public static Relation load(String text) {

        String[] lines = text.split("\n");

        int lineIndex = 0;

        // Skip blank lines and comments before the relation
        while (lineIndex < lines.length) {
            String line = lines[lineIndex].trim();

            if (!line.isEmpty() && !line.startsWith("//")) {
                break;
            }

            lineIndex++;
        }

        if (lineIndex >= lines.length) {
            throw new IllegalArgumentException(
                    "Relation definition is empty");
        }

        String header = lines[lineIndex].trim();

        int openParen = header.indexOf('(');
        int closeParen = header.indexOf(')');
        int openBrace = header.indexOf('{');

        if (openParen == -1 || closeParen == -1 || openBrace == -1) {
            throw new IllegalArgumentException(
                    "Invalid relation definition");
        }

        String name =
                header.substring(0, openParen).trim();

        String attributeText =
                header.substring(openParen + 1, closeParen);

        List<String> attributes = new ArrayList<>();

        for (String attribute : attributeText.split(",")) {
            attributes.add(attribute.trim());
        }

        Relation relation =
                new Relation(name, attributes);

        lineIndex++;

        while (lineIndex < lines.length) {

            String line = lines[lineIndex].trim();

            if (line.equals("}")) {
                return relation;
            }

            if (line.isEmpty() || line.startsWith("//")) {
                lineIndex++;
                continue;
            }

            List<Value> values = parseTuple(line);

            if (values.size() != attributes.size()) {
                throw new IllegalArgumentException(
                        "Tuple has wrong number of values");
            }

            relation.addTuple(new Tuple(values));

            lineIndex++;
        }

        throw new IllegalArgumentException(
                "Missing '}' in relation definition");
    }

    private static List<Value> parseTuple(String line) {

        List<Value> values = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        boolean insideQuotes = false;
        boolean quotedValue = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (c == '\'') {

                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '\'') {

                    current.append('\'');
                    i++;

                } else {
                    insideQuotes = !insideQuotes;
                    quotedValue = true;
                }

            } else if (c == ',' && !insideQuotes) {

                values.add(
                        createValue(
                                current.toString().trim(),
                                quotedValue));

                current.setLength(0);
                quotedValue = false;

            } else {
                current.append(c);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "Unterminated string in tuple");
        }

        values.add(
                createValue(
                        current.toString().trim(),
                        quotedValue));

        return values;
    }

    private static Value createValue(
            String text,
            boolean quoted) {

        if (quoted) {
            return new Value(text, false);
        }

        if (isNumber(text)) {
            return new Value(text, true);
        }

        return new Value(text, false);
    }

    private static boolean isNumber(String text) {

        if (text.isEmpty()) {
            return false;
        }

        int i = 0;
        boolean decimalFound = false;
        boolean digitFound = false;

        if (text.charAt(0) == '-') {
            i++;

            if (i == text.length()) {
                return false;
            }
        }

        while (i < text.length()) {

            char c = text.charAt(i);

            if (Character.isDigit(c)) {
                digitFound = true;

            } else if (c == '.' && !decimalFound) {
                decimalFound = true;

            } else {
                return false;
            }

            i++;
        }

        return digitFound;
    }
}