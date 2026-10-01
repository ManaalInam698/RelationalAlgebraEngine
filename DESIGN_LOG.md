# Design Log

## 2026-09-27
**Goal:** Understand the assignment and decide what the language should be before writing any code.

**Tried:** Read through the specification and worked out what each operator should do, including output schemas and error cases.

**What broke:** No code yet.

## 2026-09-28
**Goal:** Write GRAMMAR.md and the parse tree classes.

**Tried:** Wrote the EBNF with separate rules for each precedence level, then built the parse tree node classes and the tree printer.

**What broke:** Nothing broke in code

**AI mistake:** The AI's first grammar rule for `project` used `attribute_list`, which only allows plain identifiers. I noticed that qualified names like `Emp.Name` would not be allowed, so I changed it to a `projection_list` of `attribute_reference`.

**AI mistake:** The AI's explanation of condition precedence said `not` had the highest precedence. Checking it against the grammar, comparisons have to be evaluated before `not` can apply to them, so I corrected the precedence table to: comparisons, then `not`, then `and`, then `or`.

## 2026-09-29
**Goal:** Start writing the test cases from Section 7.

**Tried:** Wrote JUnit tests for the tokenizer and parser cases (1 to 17).

**What broke:** The missing-parenthesis test (case 16) failed

**AI mistake:** The AI initially suggested that the parser's existing error handling was sufficient for the missing-parenthesis case. Running the required JUnit test showed that the error message did not include the required input position, so I corrected the parser's `expect()` error handling to report the position at the end of the input.

## 2026-09-30

**Goal:** Finish the tests and run the performance study.

**Tried:** Wrote the semantics tests (18 to 25), the performance experiment classes, and the counters in the operators. Plotted the join on log-log axes (slope 2.03) and measured select, project and different match rates.

**What broke:** The duplicate projection test (case 24) failed.

**AI mistake:** The AI initially suggested a projection implementation that did not check for duplicate attributes. When I ran the required project[Name, Name](R) semantics test, it failed because the duplicate attribute was accepted. I corrected this by adding a check that throws a clear schema error when the same projection attribute appears more than once.

**AI mistake:** When explaining the match-rate results, the AI said the extra time "roughly quadrupled" when the output doubled, based on what theory predicts. When I checked the arithmetic myself, it was actually about 5.7 times, so I reported that instead.
