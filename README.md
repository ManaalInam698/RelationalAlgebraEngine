# Relational Algebra Engine

## How to run

Requires Java 11 and JUnit 5. Developed in IntelliJ IDEA.

- **Run a query:** set the `query` string in `Main.java` and run `main`.
- **Print a parse tree:** set the `query` string in `Parser.java` and run its `main`.
- **Run the tests:** right-click the `tests` folder and choose **Run 'All Tests'**. This runs both `TokenizerParserTest` (cases 1 to 17) and `SemanticsTest` (cases 18 to 25).
- **Generate data:** run `DataGenerator` to write `R.txt` and `S.txt`.
- **Run the experiments:** run `PerformanceExperiment`, `SelectProjectExperiment` or `MatchRateExperiment`. Results are in REPORT.md.


## What is supported

- `select`, `project`, `rename`, `union`, `intersect`, `minus`, `times` and `join`, with the precedence and associativity described in GRAMMAR.md.
- Conditions with `=`, `!=`, `<`, `<=`, `>`, `>=`, `and`, `or`, `not`, parentheses, and qualified names like `Emp.DID`.
- Quoted strings, with `''` for a literal quote.
- Keywords can be used as attribute names, like `select[union=3](R)`, except `not`.
- `project[Name, Name](R)` is rejected with a schema error.
- Lexical, syntax, name, schema and type errors, with positions for lexical and syntax errors.
- Self joins with `rename`. Without it, both copies of `Emp` would have the same name, so `Emp.EID` could not tell the two copies apart.

## Known limitations

- No command-line interface; queries are set in `Main` or `Parser`.
- Duplicate removal scans the whole relation, which makes `project` quadratic.
- Joining a relation with itself without `rename` does not report an error.
- Attributes must be unqualified on base relations and qualified after a join or `times`.
- Union compatibility only checks attribute names, not types.
- `DataGenerator` does not produce an exact match rate.
