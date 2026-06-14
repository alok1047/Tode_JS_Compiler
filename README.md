# ⚡ Tode (Powered by ThunderJS)

Tode is a professional, developer-oriented JavaScript runtime written from scratch in Java 21, featuring rich error diagnostics, smart suggestions, call-stack tracing, narration-based Explain Mode, and a built-in Date subsystem. Built entirely without third-party dependencies, it serves as an educational and inspection-friendly execution engine.

---

## 🚀 Quick Start & Execution

### 1. Build the Executable JAR
First, compile and package the runtime using the build script:
```bash
bash src/build.sh
```
This compiles the source code and packages it into `tode.jar` in the root folder.

### 2. Running Files
Execute any JavaScript file directly using the packaged JAR:
```bash
java -jar tode.jar tests/test1_oddeven.js
```

### 3. Interactive REPL
Launch the interactive shell by running the JAR with no arguments:
```bash
java -jar tode.jar
```

### 4. Running Tests
Run integration tests or the 50-program stress suite directly from the packaged JAR:
```bash
# Run integration regression tests
java -cp tode.jar tests.TestRunner

# Run the 50-program stress suite
java -cp tode.jar tests.StressTester
```

---

## 🛠️ CLI Introspection Flags

Evaluate code with developer diagnostics:
* `java -jar tode.jar --ast file.js` — Formats and prints parsed AST as a Unicode tree.
* `java -jar tode.jar --trace file.js` — Prints step-by-step execution trace, variables, and function calls.
* `java -jar tode.jar --explain file.js` — Narrates code execution in plain English.
* `java -jar tode.jar --coverage file.js` — Shows a summary of language features used.
* `java -jar tode.jar --bench file.js` — Reports execution time in milliseconds.
* `java -jar tode.jar --format file.js` — Pretty-prints / auto-formats JS source code via AST.
* `java -jar tode.jar --minify file.js` — Outputs minified (compressed) JS source code.

---

## ✨ Features Matrix

* **Variables & Scope**: Lexical block scoping (`let`, `const`, `var`), variable shadowing, and constant enforcement.
* **Operators**: Arithmetic, comparisons, logical, assignment, postfix/prefix update (`++`/`--`), and ternary operators.
* **Control Flow**: `if`/`else`, `switch/case/default`, `for`, `while`, `do-while` loops (supports `break`/`continue`).
* **Functions**: Closures, arrow functions, default parameters, rest parameters (`...`), and nested calls.
* **Data Types**: Native representation of arrays (includes `map`/`filter`/`reduce`/`sort` etc.), strings (template literals), and object literals.
* **Date Subsystem**: Production-grade JS-compliant `Date` constructor, getters/setters, static methods, and ISO/UTC string formatting.

---

## 🏗️ Repository Structure & Runtime Pipeline

```
Tode/
├── src/               # Core runtime source code (Java)
│   ├── thunderjs/     # Core engine (lexer, parser, interpreter, runtime, builtins, etc.)
│   ├── docs/          # Validation report
│   ├── build.sh       # Compile and JAR packaging script
│   └── tode           # Development launcher script
├── features/          # Developer introspection features (Java)
│   └── (ASTPrinter, Formatter, Minifier, TraceEngine, ExplainEngine, CoverageTracker)
├── tests/             # JavaScript test files, reports, and Java test classes
│   ├── errors/        # Error diagnostic test cases
│   ├── TestRunner.java # Integration test runner (Java)
│   ├── StressTester.java # 50-program stress suite runner (Java)
│   └── hidden_test_report.md # Generated stress suite execution report
├── README.md          # Project documentation
└── tode.jar           # Packaged executable runtime JAR (generated)
```

```
[JS Source Code] ──► [Lexical Scanner] ──► [AST Parser] ──► [Introspection] ──► [Interpreter] ──► [stdout]
```

---

## 💡 Rich Error Diagnostics & Intelligent Suggestions

Tode includes a compiler-grade diagnostics system and local smart suggestions to help developers identify and fix bugs:
* **Rich Diagnostic Output**: Every syntax error, lexer error, and runtime error displays with error type, message, filename, line, column, source code snippet, and a caret range pointing to the error site.
* **Call Stack Tracing**: On runtime errors, a complete call stack trace is printed (innermost first) detailing function name, file, and line/column.
* **Intelligent Typo Suggestions**: Automatically detects typos in variable names, global builtins, custom functions, and object properties (e.g. on `Math`, `Object`, `console`, and `Date` APIs) using Levenshtein distance (<= 2).

### Example Diagnostic:
```text
ReferenceError: usernme is not defined
  File: login.js
  Line: 18, Column: 15

  18 | console.log(usernme);
                   ^^^^^^^

  💡 Did you mean: username
```