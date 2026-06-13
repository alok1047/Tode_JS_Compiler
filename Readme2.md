⚡ Tode

A JavaScript runtime written entirely in Java.

Tode implements its own Lexer, Parser, AST, and Interpreter without using Node.js, V8, Rhino, Nashorn, GraalJS, or any existing JavaScript engine.

Run

bash tode examples/test1_oddeven.js

Developer tools:

bash tode --tokens file.js
bash tode --ast file.js
bash tode --trace file.js
bash tode --explain file.js
bash tode --repl

⸻

Features

✅ Variables (let, const, var)

✅ Numbers, Strings, Booleans, Null, Undefined

✅ Objects & Arrays

✅ Functions, Closures, Arrow Functions

✅ Arithmetic, Comparison & Logical Operators

✅ if / else / switch

✅ for / while / do-while

✅ Array Methods
map, filter, reduce, find, some, every

✅ String Methods
split, replace, substring, slice, trim, includes, startsWith, endsWith

✅ Math Object
floor, ceil, pow, sqrt, random

✅ Spread & Rest Operators

✅ Interactive REPL

✅ AST Visualization

✅ Execution Tracing

✅ Explain Mode

⸻

Runtime Pipeline

JavaScript Source
        ↓
      Lexer
        ↓
     Tokens
        ↓
      Parser
        ↓
       AST
        ↓
   Interpreter
        ↓
      Output

⸻

Project Structure

src/
├── lexer/
├── parser/
├── ast/
├── interpreter/
├── runtime/
├── builtins/
└── debugger/

⸻

Example

function add(a, b) {
    return a + b;
}
console.log(add(5, 7));

Output:

12

⸻

Validation

* 5/5 Official Test Cases Passed
* 50/50 Stress Tests Passed
* Custom JavaScript Runtime
* No External JS Engine Used