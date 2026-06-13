package thunderjs;

import thunderjs.ast.Stmt;
import thunderjs.interpreter.Interpreter;
import thunderjs.lexer.Lexer;
import thunderjs.lexer.Token;
import thunderjs.parser.Parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StressTester {
    static class TestCase {
        final String name;
        final String source;
        final String expected;
        final boolean expectError;

        TestCase(String name, String source, String expected, boolean expectError) {
            this.name = name;
            this.source = source;
            this.expected = expected;
            this.expectError = expectError;
        }
    }

    public static void main(String[] args) {
        List<TestCase> tests = new ArrayList<>();

        // 1. Arithmetic precedence
        tests.add(new TestCase("1. Arithmetic precedence",
            "console.log(2 + 3 * 4 ** 2 - 10 / 2);",
            "45\n", false));

        // 2. Variable shadowing
        tests.add(new TestCase("2. Variable shadowing",
            "let x = 10; { let x = 20; console.log(x); } console.log(x);",
            "20\n10\n", false));

        // 3. Constant reassignment error
        tests.add(new TestCase("3. Constant reassignment error",
            "const x = 10; x = 20;",
            "TypeError: Assignment to constant variable 'x'", true));

        // 4. Missing variable reference error
        tests.add(new TestCase("4. Missing variable reference error",
            "console.log(y);",
            "ReferenceError: y is not defined", true));

        // 5. Function parameter shadowing
        tests.add(new TestCase("5. Function parameter shadowing",
            "let x = 5; function f(x) { console.log(x); } f(10); console.log(x);",
            "10\n5\n", false));

        // 6. Closure variable capture
        tests.add(new TestCase("6. Closure variable capture",
            "function f() { let x = 10; return () => { x += 5; return x; }; } let g = f(); console.log(g()); console.log(g());",
            "15\n20\n", false));

        // 7. Recursion (factorial)
        tests.add(new TestCase("7. Recursion (factorial)",
            "function fact(n) { if (n <= 1) return 1; return n * fact(n - 1); } console.log(fact(5));",
            "120\n", false));

        // 8. Recursion (Fibonacci)
        tests.add(new TestCase("8. Recursion (Fibonacci)",
            "function fib(n) { if (n <= 1) return n; return fib(n - 1) + fib(n - 2); } console.log(fib(7));",
            "13\n", false));

        // 9. Array push/pop/shift/unshift
        tests.add(new TestCase("9. Array push/pop/shift/unshift",
            "let a = [2]; a.push(3); a.unshift(1); console.log(a.join(',')); console.log(a.pop()); console.log(a.shift()); console.log(a.join(','));",
            "1,2,3\n3\n1\n2\n", false));

        // 10. Array reverse/join
        tests.add(new TestCase("10. Array reverse/join",
            "let a = [1, 2, 3]; a.reverse(); console.log(a.join('-'));",
            "3-2-1\n", false));

        // 11. Array includes/indexOf/lastIndexOf
        tests.add(new TestCase("11. Array includes/indexOf/lastIndexOf",
            "let a = [1, 2, 3, 2]; console.log(a.includes(2)); console.log(a.indexOf(2)); console.log(a.lastIndexOf(2));",
            "true\n1\n3\n", false));

        // 12. Array slice/splice/concat
        tests.add(new TestCase("12. Array slice/splice/concat",
            "let a = [1, 2, 3, 4]; console.log(a.slice(1, 3).join(',')); a.splice(1, 2, 8, 9); console.log(a.join(',')); console.log(a.concat([5, 6]).join(','));",
            "2,3\n1,8,9,4\n1,8,9,4,5,6\n", false));

        // 13. Array sort (default and custom comparator)
        tests.add(new TestCase("13. Array sort",
            "let a = [10, 2, 30, 4]; a.sort((x, y) => x - y); console.log(a.join(','));",
            "2,4,10,30\n", false));

        // 14. Higher-order array map
        tests.add(new TestCase("14. Higher-order array map",
            "let a = [1, 2, 3]; console.log(a.map(x => x * 3).join(','));",
            "3,6,9\n", false));

        // 15. Higher-order array filter
        tests.add(new TestCase("15. Higher-order array filter",
            "let a = [1, 2, 3, 4]; console.log(a.filter(x => x % 2 !== 0).join(','));",
            "1,3\n", false));

        // 16. Higher-order array reduce
        tests.add(new TestCase("16. Higher-order array reduce",
            "let a = [1, 2, 3, 4]; console.log(a.reduce((sum, x) => sum + x, 10));",
            "20\n", false));

        // 17. Higher-order array find/findIndex
        tests.add(new TestCase("17. Higher-order array find/findIndex",
            "let a = [10, 20, 30]; console.log(a.find(x => x > 15)); console.log(a.findIndex(x => x > 15));",
            "20\n1\n", false));

        // 18. Higher-order array some/every/forEach
        tests.add(new TestCase("18. Higher-order array some/every/forEach",
            "let a = [1, 2, 3]; console.log(a.some(x => x > 2)); console.log(a.every(x => x > 0)); let sum = 0; a.forEach(x => { sum += x; }); console.log(sum);",
            "true\ntrue\n6\n", false));

        // 19. Array flat/fill
        tests.add(new TestCase("19. Array flat/fill",
            "let a = [1, [2, 3]]; console.log(a.flat().join(',')); let b = [1, 2, 3]; b.fill(9, 1, 3); console.log(b.join(','));",
            "1,2,3\n1,9,9\n", false));

        // 20. String length and case methods
        tests.add(new TestCase("20. String length and case methods",
            "let s = 'Hello'; console.log(s.length); console.log(s.toUpperCase()); console.log(s.toLowerCase());",
            "5\nHELLO\nhello\n", false));

        // 21. String trim/trimStart/trimEnd
        tests.add(new TestCase("21. String trim/trimStart/trimEnd",
            "let s = ' hello '; console.log(s.trim()); console.log(s.trimStart()); console.log(s.trimEnd());",
            "hello\nhello \n hello\n", false));

        // 22. String split/join combination
        tests.add(new TestCase("22. String split/join combination",
            "let s = 'a,b,c'; console.log(s.split(',').join('|'));",
            "a|b|c\n", false));

        // 23. String replace/replaceAll
        tests.add(new TestCase("23. String replace/replaceAll",
            "let s = 'hello hello'; console.log(s.replace('hello', 'hi')); console.log(s.replaceAll('hello', 'hi'));",
            "hi hello\nhi hi\n", false));

        // 24. String substring/slice
        tests.add(new TestCase("24. String substring/slice",
            "let s = 'javascript'; console.log(s.substring(4, 10)); console.log(s.slice(-6));",
            "script\nscript\n", false));

        // 25. String indexOf/includes/startsWith/endsWith
        tests.add(new TestCase("25. String indexOf/includes/startsWith/endsWith",
            "let s = 'hello world'; console.log(s.indexOf('o')); console.log(s.includes('world')); console.log(s.startsWith('he')); console.log(s.endsWith('ld'));",
            "4\ntrue\ntrue\ntrue\n", false));

        // 26. String charAt/charCodeAt/repeat
        tests.add(new TestCase("26. String charAt/charCodeAt/repeat",
            "let s = 'abc'; console.log(s.charAt(1)); console.log(s.charCodeAt(0)); console.log(s.repeat(3));",
            "b\n97\nabcabcabc\n", false));

        // 27. String padStart/padEnd/concat
        tests.add(new TestCase("27. String padStart/padEnd/concat",
            "let s = '5'; console.log(s.padStart(3, '0')); console.log(s.padEnd(3, '0')); console.log(s.concat('a', 'b'));",
            "005\n500\n5ab\n", false));

        // 28. Object literal simple access
        tests.add(new TestCase("28. Object literal simple access",
            "let o = { x: 1, y: 2 }; console.log(o.x); console.log(o.y);",
            "1\n2\n", false));

        // 29. Object property assignment
        tests.add(new TestCase("29. Object property assignment",
            "let o = {}; o.x = 42; console.log(o.x);",
            "42\n", false));

        // 30. Object computed property access
        tests.add(new TestCase("30. Object computed property access",
            "let o = { a: 10 }; let k = 'a'; console.log(o[k]); o[k] = 20; console.log(o.a);",
            "10\n20\n", false));

        // 31. Object.keys/values/entries
        tests.add(new TestCase("31. Object.keys/values/entries",
            "let o = { a: 1, b: 2 }; console.log(Object.keys(o).join(',')); console.log(Object.values(o).join(','));",
            "a,b\n1,2\n", false));

        // 32. Switch case matching
        tests.add(new TestCase("32. Switch case matching",
            "let x = 'a'; switch(x) { case 'a': console.log(1); break; case 'b': console.log(2); break; }",
            "1\n", false));

        // 33. Switch case fallthrough
        tests.add(new TestCase("33. Switch case fallthrough",
            "let x = 'a'; switch(x) { case 'a': console.log(1); case 'b': console.log(2); break; }",
            "1\n2\n", false));

        // 34. Switch case default fallback
        tests.add(new TestCase("34. Switch case default fallback",
            "let x = 'z'; switch(x) { case 'a': console.log(1); break; default: console.log(3); }",
            "3\n", false));

        // 35. Do-while loop execution
        tests.add(new TestCase("35. Do-while loop execution",
            "let i = 0; do { console.log(i); i++; } while (i < 2);",
            "0\n1\n", false));

        // 36. Break in for loop
        tests.add(new TestCase("36. Break in for loop",
            "for (let i = 0; i < 5; i++) { if (i === 3) break; console.log(i); }",
            "0\n1\n2\n", false));

        // 37. Continue in for loop
        tests.add(new TestCase("37. Continue in for loop",
            "for (let i = 0; i < 5; i++) { if (i === 3) continue; console.log(i); }",
            "0\n1\n2\n4\n", false));

        // 38. Nested loops (for inside while)
        tests.add(new TestCase("38. Nested loops (for inside while)",
            "let i = 0; while (i < 2) { for (let j = 0; j < 2; j++) { console.log(i + ',' + j); } i++; }",
            "0,0\n0,1\n1,0\n1,1\n", false));

        // 39. Ternary operator true branch
        tests.add(new TestCase("39. Ternary operator true branch",
            "console.log(true ? 'yes' : 'no');",
            "yes\n", false));

        // 40. Ternary operator false branch
        tests.add(new TestCase("40. Ternary operator false branch",
            "console.log(false ? 'yes' : 'no');",
            "no\n", false));

        // 41. Loose vs strict equality
        tests.add(new TestCase("41. Loose vs strict equality",
            "console.log('5' == 5); console.log('5' === 5);",
            "true\nfalse\n", false));

        // 42. Loose vs strict inequality
        tests.add(new TestCase("42. Loose vs strict inequality",
            "console.log('5' != 5); console.log('5' !== 5);",
            "false\ntrue\n", false));

        // 43. Loose equality for null/undefined
        tests.add(new TestCase("43. Loose equality for null/undefined",
            "console.log(null == undefined); console.log(null === undefined);",
            "true\nfalse\n", false));

        // 44. Loose equality for truthy/falsy coercion
        tests.add(new TestCase("44. Loose equality for truthy/falsy coercion",
            "console.log(0 == ''); console.log(0 == false);",
            "true\ntrue\n", false));

        // 45. Prefix/postfix increment/decrement
        tests.add(new TestCase("45. Prefix/postfix increment/decrement",
            "let x = 5; console.log(x++); console.log(x); console.log(++x); console.log(x--); console.log(x); console.log(--x);",
            "5\n6\n7\n7\n6\n5\n", false));

        // 46. Compound assignment
        tests.add(new TestCase("46. Compound assignment",
            "let x = 10; x += 5; console.log(x); x -= 3; console.log(x); x *= 2; console.log(x); x /= 4; console.log(x);",
            "15\n12\n24\n6\n", false));

        // 47. Logical short-circuiting
        tests.add(new TestCase("47. Logical short-circuiting",
            "let x = 0; console.log(true || (x = 5)); console.log(x); console.log(false && (x = 10)); console.log(x);",
            "true\n0\nfalse\n0\n", false));

        // 48. Typeof operator checks
        tests.add(new TestCase("48. Typeof operator checks",
            "console.log(typeof 42); console.log(typeof 'hi'); console.log(typeof true); console.log(typeof undefined); console.log(typeof {});",
            "number\nstring\nboolean\nundefined\nobject\n", false));

        // 49. parseInt and parseFloat
        tests.add(new TestCase("49. parseInt and parseFloat",
            "console.log(parseInt('101', 2)); console.log(parseFloat('3.14'));",
            "5\n3.14\n", false));

        // 50. Template literals with interpolation
        tests.add(new TestCase("50. Template literals with interpolation",
            "let name = 'Grace'; let age = 30; console.log(`Hello, ${name}! Next year you will be ${age + 1}.`);",
            "Hello, Grace! Next year you will be 31.\n", false));

        // Start running tests
        int passed = 0;
        int failed = 0;
        StringBuilder report = new StringBuilder();

        report.append("# Hidden-Test Stress Suite Report — ThunderJS\n\n");
        report.append("This report details the execution results of a 50-program stress suite designed to validate execution correctness and find edge cases in the ThunderJS interpreter.\n\n");

        for (TestCase tc : tests) {
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outStream));
            System.setErr(new PrintStream(errStream));

            boolean actualErrorThrown = false;
            String errorMsg = "";

            try {
                Lexer lexer = new Lexer(tc.source);
                List<Token> tokens = lexer.tokenize();
                Parser parser = new Parser(tokens);
                List<Stmt> statements = parser.parse();
                Interpreter interpreter = new Interpreter();
                interpreter.interpret(statements);
            } catch (Exception e) {
                actualErrorThrown = true;
                errorMsg = e.getMessage();
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }

            String actual = outStream.toString().replace("\r\n", "\n");
            String actualErr = errStream.toString().replace("\r\n", "\n");

            boolean success = false;
            if (tc.expectError) {
                if (actualErrorThrown && errorMsg != null && errorMsg.contains(tc.expected)) {
                    success = true;
                } else if (actualErr.contains(tc.expected)) {
                    success = true;
                }
            } else {
                if (!actualErrorThrown && actual.equals(tc.expected)) {
                    success = true;
                }
            }

            if (success) {
                passed++;
            } else {
                failed++;
            }

            report.append("## ").append(tc.name).append("\n");
            report.append("* **Source Code:**\n```javascript\n").append(tc.source).append("\n```\n");
            report.append("* **Expected Output:**\n```\n").append(tc.expected).append("\n```\n");
            report.append("* **Actual Output:**\n```\n").append(tc.expectError && actualErrorThrown ? errorMsg : (tc.expectError ? actualErr : actual)).append("\n```\n");
            report.append("* **Result:** ").append(success ? "PASSED ✅" : "FAILED ❌").append("\n\n");
            report.append("---\n\n");
        }

        report.append("## Final Summary\n");
        report.append("* **Total Tests:** ").append(tests.size()).append("\n");
        report.append("* **Passed:** ").append(passed).append("\n");
        report.append("* **Failed:** ").append(failed).append("\n");
        report.append("* **Success Rate:** ").append((double) passed / tests.size() * 100).append("%\n");

        try (FileWriter writer = new FileWriter("/Users/alokagrahari/.gemini/antigravity/brain/681a317c-462d-45e5-bc23-ffca186826c5/hidden_test_report.md")) {
            writer.write(report.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("Stress Tester Done: %d passed, %d failed out of %d total.\n", passed, failed, tests.size());
    }
}
