# Relational Algebra Grammar

## 5.1 Grammar
```
program ::= { relation_definition } [ expression ]

relation_definition ::= identifier "(" attribute_list ")" "=" "{" { tuple } "}"

attribute_list ::= identifier { "," identifier }

tuple ::= value { "," value }

value ::= number | string

expression ::= set_expression

set_expression ::= product_expression
{ ("union" | "intersect" | "minus") product_expression }

product_expression ::= unary_expression
{ ("times" unary_expression)
| ("join" "[" condition "]" unary_expression) }

unary_expression ::= identifier
| select_expression
| project_expression
| rename_expression
| "(" expression ")"

select_expression ::= "select" "[" condition "]" "(" expression ")"

project_expression ::= "project" "[" projection_list "]" "(" expression ")"

projection_list ::= attribute_reference { "," attribute_reference }
rename_expression ::= "rename" "[" identifier "]" "(" expression ")"

condition ::= or_condition

or_condition ::= and_condition { "or" and_condition }

and_condition ::= not_condition { "and" not_condition }

not_condition ::= "not" not_condition
| condition_primary

condition_primary ::= comparison
| "(" condition ")"

comparison ::= operand comparison_operator operand

comparison_operator ::= "=" | "!=" | "<" | "<=" | ">" | ">="

operand ::= number | quoted_string | attribute_reference

attribute_reference ::= identifier [ "." identifier ]

identifier ::= letter { letter | digit | "_" }

number ::= ["-"] digit { digit } [ "." digit { digit } ]

letter ::= "A".."Z" | "a".."z"

digit ::= "0".."9"

string ::= bare_string | quoted_string

bare_string ::= bare_char { bare_char }
bare_char ::= any character except comma, whitespace, "(", ")", or "'"

quoted_string ::= "'" { quoted_char | "''" } "'"

quoted_char ::= any character except "'"
```

### Lexical Rules
- Within a relation definition, each non-blank line between { and } represents one tuple.
- Whitespace is ignored between query tokens.
- Blank lines are ignored.
- `//` begins a comment that continues to the end of the line.
- Inside a quoted string, whitespace and special characters are preserved.
- Two consecutive single quotes `''` inside a quoted string represent one literal single quote.
- Keywords are not reserved. The tokenizer reads words like `union` and `and` as ordinary identifiers, and the parser only treats a word as a keyword where the grammar expects that keyword. Inside a condition the parser expects an operand, so in `select[union=3](R)` the word `union` is read as an attribute name. The one exception is `not`, which is always read as a keyword at the start of a condition.
- In queries, strings must be quoted. A bare word is always an attribute name, so in `select[A=B](R)`, `B` is an attribute. Bare strings are only allowed inside relation definitions.


## 5.2 Precedence and Associativity

The relational operators use the following precedence and associativity rules:

| Precedence | Operators | Associativity | Enforced by |
|---|---|---|---|
| Highest | select, project, rename, parentheses | N/A | `unary_expression` |
| 2 | join, times | Left | `product_expression` |
| Lowest | union, intersect, minus | Left | `set_expression` |

For conditions:

| Precedence | Operator | Associativity | Enforced by |
|---|---|---|---|
| Highest | parentheses | N/A | `condition_primary` |
| 4 | =, !=, <, <=, >, >= | Non-associative | `comparison` |
| 3 | not | Right | `not_condition` |
| 2 | and | Left | `and_condition` |
| Lowest | or | Left | `or_condition` |

Each level's rule is built from the level above it, so higher levels group first. Left associativity comes from the `{ ... }` repetition: the parser reads operators left to right, and each new operator takes everything parsed so far as its left operand.

The binary operators at the same precedence level associate from left to right. Therefore:

`A union B minus C`

is interpreted as:

`(A union B) minus C`

Similarly:

`A minus B minus C`

is interpreted as:

`(A minus B) minus C`

### Duplicate Projection Attributes

Duplicate attributes are not allowed in a projection.

For example:

`project[Name, Name](R)`

produces a schema error because `Name` appears more than once in the
projection list.

### Minus Associativity Example

To show why associativity matters, let:

A = {1, 2}
B = {2}
C = {1}

Using the left-associative rule:

(A minus B) minus C

A minus B = {1}

{1} minus {1} = {}

Result: {}

If minus were right-associative:

A minus (B minus C)

B minus C = {2}

{1, 2} minus {2} = {1}

Result: {1}

Therefore, the two groupings produce different results. My grammar uses the
left-associative interpretation: (A minus B) minus C.

## 5.3 Ambiguity Demonstration

The naive grammar is:

```
Expr ::= Expr "union" Expr
| Expr "minus" Expr
| "(" Expr ")"
| IDENT
```

For the input:

`A union B minus C`

the grammar allows two different interpretations.

### Parse Tree 1: (A union B) minus C
```
        minus
       /     \
    union     C
    /   \
   A     B
```
### Parse Tree 2: A union (B minus C)
```
       union 
       /   \
      A    minus
           /   \
          B     C
```

### Example Showing Different Results

Let:

A = {1}
B = {2}
C = {1}

For the first interpretation:

`(A union B) minus C`

A union B = {1, 2}

{1, 2} minus {1} = {2}

Result: `{2}`

For the second interpretation:

`A union (B minus C)`

B minus C = {2}

{1} union {2} = {1, 2}

Result: `{1, 2}`

Therefore, the two parse trees produce different results.




### Removing the Ambiguity

The stratified grammar removes this ambiguity:

```
expression ::= set_expression

set_expression ::= product_expression
                   { ("union" | "intersect" | "minus") product_expression }

product_expression ::= unary_expression
                       { ("times" unary_expression)
                       | ("join" "[" condition "]" unary_expression) }

unary_expression ::= identifier
                   | select_expression
                   | project_expression
                   | rename_expression
                   | "(" expression ")"
```

Operators at the same precedence level are processed from left to right.

Therefore:

`A union B minus C`

is interpreted as:

`(A union B) minus C`

This forces Parse Tree 1 and prevents the ambiguous second interpretation.


## 5.4 Parsing Strategy

I will use a recursive descent parser. I chose recursive descent because the grammar is divided into clear precedence levels, so each grammar rule can be implemented using a corresponding parsing method.

Left recursion is a problem for recursive descent parsers because a rule such as:
```
Expr ::= Expr "union" Expr
```

would cause the parser to call itself repeatedly without consuming any input, resulting in infinite recursion.

I removed left recursion in my stratified grammar. For example, instead of using a left-recursive rule for union, intersect, and minus, I use:

```
set_expression ::= product_expression
                   { ("union" | "intersect" | "minus") product_expression }
```

Similarly, times and join are handled with:

```
product_expression ::= unary_expression
                       { ("times" unary_expression)
                       | ("join" "[" condition "]" unary_expression) }
```

These rules allow the recursive descent parser to consume the left operand first and then process additional operators and operands from left to right without left recursion.


## 5.5 Sources

### Sources Used

- Course lecture notes and assignment specification.
- **Wikipedia, "Operator-precedence grammar"** (https://en.wikipedia.org/wiki/Operator-precedence_grammar)
- **Wikipedia, "Maximal munch"** (https://en.wikipedia.org/wiki/Maximal_munch)
- AI assistance (ChatGPT) was used to help understand EBNF, parsing, precedence, and ambiguity. Its suggestions were checked against the assignment requirements rather than accepted automatically

### AI Assistance Corrections

One correction was made to the initial grammar for `project`. It originally used:

project_expression ::= "project" "[" attribute_list "]" "(" expression ")"

This was changed to use `projection_list` containing `attribute_reference`, allowing qualified attributes such as `Emp.Name`.

Another correction was made to the condition precedence explanation. The initial explanation described `not` as having the highest operator precedence, but comparisons must be evaluated before `not`, followed by `and` and then `or`.

