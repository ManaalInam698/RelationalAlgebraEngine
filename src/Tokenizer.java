import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private String input;
    private int position;

    public Tokenizer(String input) {
        this.input = input;
        this.position = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {

            char currentChar = input.charAt(position);

            // to Ignore whitespace
            if (Character.isWhitespace(currentChar)) {
                position++;
                continue;
            }

            // Ignore comments beginning with //
            if (currentChar == '/' && position + 1 < input.length()
                    && input.charAt(position + 1) == '/') {

                while (position < input.length()
                        && input.charAt(position) != '\n') {
                    position++;
                }
                continue;
            }

            // Identifiers and keywords
            if (Character.isLetter(currentChar)) {
                int start = position;


                while (position < input.length()
                        && (Character.isLetterOrDigit(input.charAt(position))
                        || input.charAt(position) == '_')) {
                    position++;
                }

                String value = input.substring(start, position);
                tokens.add(new Token("IDENTIFIER", value, start));
                continue;
            }

            // Numbers, including negative numbers
            if (Character.isDigit(currentChar)
                    || (currentChar == '-' && position + 1 < input.length()
                    && Character.isDigit(input.charAt(position + 1)))) {

                int start = position;

                if (currentChar == '-') {
                    position++;
                }

                while (position < input.length()
                        && Character.isDigit(input.charAt(position))) {
                    position++;
                }

                if (position < input.length()
                        && input.charAt(position) == '.') {

                    position++;

                    while (position < input.length()
                            && Character.isDigit(input.charAt(position))) {
                        position++;
                    }
                }

                String value = input.substring(start, position);
                tokens.add(new Token("NUMBER", value, start));
                continue;
            }

            // Quoted strings
            if (currentChar == '\'') {
                int start = position;
                position++;

                StringBuilder value = new StringBuilder();
                boolean closed = false;

                while (position < input.length()) {

                    if (input.charAt(position) == '\'') {

                        // Two single quotes mean one literal quote
                        if (position + 1 < input.length()
                                && input.charAt(position + 1) == '\'') {
                            value.append('\'');
                            position += 2;
                        } else {
                            position++;
                            closed = true;
                            break;
                        }

                    } else {
                        value.append(input.charAt(position));
                        position++;
                    }
                }


                if (!closed) {
                    throw new IllegalArgumentException(
                            "Unterminated string at position " + start);
                }

                tokens.add(new Token("STRING", value.toString(), start));
                continue;
            }

            // Maximal munch for comparison operators
            if (currentChar == '>' || currentChar == '<'
                    || currentChar == '!' || currentChar == '=') {

                int start = position;
                if (currentChar == '!'
                        && (position + 1 >= input.length()
                        || input.charAt(position + 1) != '=')) {

                    throw new IllegalArgumentException(
                            "Invalid operator '!' at position " + position);
                }
                String operator = String.valueOf(currentChar);
                position++;

                if (position < input.length()
                        && input.charAt(position) == '=') {
                    operator += "=";
                    position++;
                }

                tokens.add(new Token("OPERATOR", operator, start));
                continue;
            }

            // Single-character symbols
            if ("[](){},.".indexOf(currentChar) >= 0) {
                tokens.add(new Token("SYMBOL",
                        String.valueOf(currentChar), position));
                position++;
                continue;
            }

            throw new IllegalArgumentException(
                    "Unexpected character '" + currentChar
                            + "' at position " + position);
        }

        return tokens;
    }


    public static void main(String[] args) {

        Tokenizer tokenizer =
                new Tokenizer("select[Age!=30](Employees)");

        List<Token> tokens = tokenizer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}