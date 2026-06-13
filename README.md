# ⚡ Tode (Powered by ThunderJS)

Tode is a high-performance, custom JavaScript runtime written from scratch in Java 21, featuring interactive AST visualization, call-stack tracing, narration-based Explain Mode, and a built-in Date subsystem. Built entirely without third-party dependencies, it serves as an educational and inspection-friendly execution engine.

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
java -jar tode.jar examples/test1_oddeven.js
```

### 3. Interactive REPL
Launch the interactive shell by running the JAR with no arguments:
```bash
java -jar tode.jar
```

---

## 🛠️ CLI Introspection Flags

Evaluate code with developer diagnostics:
* `java -jar tode.jar --tokens file.js` — Prints lexer token stream.
* `java -jar tode.jar --ast file.js` — Formats and prints parsed AST as a Unicode tree.
* `java -jar tode.jar --trace file.js` — Prints step-by-step execution trace & scope frames.
* `java -jar tode.jar --explain file.js` — Narrates code execution in plain English.
* `java -jar tode.jar --visual file.js` — Groups outputs into Variables, Expressions, and Console output.
* `java -jar tode.jar --html file.js` — Generates collapsible, dark-themed HTML AST visualizer.
* `java -jar tode.jar --coverage file.js` — Shows a summary of language features used.
* `java -jar tode.jar --bench file.js` — Reports execution time in milliseconds.

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
ThunderJS/
├── src/               # Java source code, build script, validation docs, launcher
│   ├── thunderjs/     # Main package files
│   ├── docs/          # Validation report
│   ├── build.sh       # Compile and JAR packaging script
│   └── tode           # Development launcher script
├── examples/          # Example JS programs and validation tests
├── README.md          # Project documentation
└── tode.jar           # Packaged executable runtime JAR (generated)
```

```
[JS Source Code] ──► [Lexical Scanner] ──► [AST Parser] ──► [Introspection] ──► [Interpreter] ──► [stdout]
```