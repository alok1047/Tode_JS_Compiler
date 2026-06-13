# ThunderJS — Executable JAR Validation Report

This report demonstrates and verifies the execution of Tode/ThunderJS using the packaged executable `tode.jar`.

---

## 1. Normal Execution
Run the odd/even test case to verify correct tree-walk evaluation.

### Command:
```bash
java -jar tode.jar examples/test1_oddeven.js
```

### Output:
```text
7 is Odd
```

---

## 2. AST Introspection Mode
Evaluate and print the parsed Abstract Syntax Tree formatting.

### Command:
```bash
java -jar tode.jar --ast examples/test1_oddeven.js
```

### Output:
```text
🌳 ThunderJS AST

Program
├── let num = 7
└── if (num % 2 === 0)
    ├── Block
    │   └── console.log(num + " is Even")
    └── Block
        └── console.log(num + " is Odd")
```

---

## 3. Scope and Execution Trace Mode
Print the execution steps and local environment frame variables.

### Command:
```bash
java -jar tode.jar --trace examples/test1_oddeven.js
```

### Output:
```text
⚡ ThunderJS Execution Trace

[Step 1]
Execute:
let num = 7

Result:
7

Environment:
{
  Date: function () { [native code] },
  num: 7
}

[Step 2]
Execute:
if (num % 2 === 0) { console.log(num + " is Even"); } else { console.log(num + " is Odd"); }

[Step 3]
Execute:
console.log(num + " is Odd")

Output:
7 is Odd

Environment:
{
  Date: function () { [native code] },
  num: 7
}

Environment:
{
  Date: function () { [native code] },
  num: 7
}
```

---

## 4. Narration and Explain Mode
Narrate the execution log statement-by-statement in plain English.

### Command:
```bash
java -jar tode.jar --explain examples/test1_oddeven.js
```

### Output:
```text
🧠 ThunderJS Explain Mode

[Line 1]
Create variable:
num = 7

[Line 2]
Calculate:
num % 2

Result:
1
Calculate:
num % 2 === 0

Result:
false
Evaluate if condition:
num % 2 === 0

Result:
False (else branch taken)

[Line 5]
Calculate:
num + " is Odd"

Result:
7 is Odd
Print:
7 is Odd
```
