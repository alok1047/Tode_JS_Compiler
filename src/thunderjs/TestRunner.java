package thunderjs;

import thunderjs.ast.Stmt;
import thunderjs.interpreter.Interpreter;
import thunderjs.lexer.Lexer;
import thunderjs.lexer.Token;
import thunderjs.parser.Parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TestRunner {
    private static final Map<String, String> EXPECTED_OUTPUTS = new TreeMap<>(Map.of(
        "examples/test1_oddeven.js", "7 is Odd\n",
        "examples/test2_triangle.js", "*\n**\n***\n****\n*****\n",
        "examples/test3_armstrong.js", "true\nfalse\n",
        "examples/test4_array.js", "1 2 3 4 5\n5 4 3 2 1\n",
        "examples/test5_palindrome.js", "racecar is a palindrome\n"
    ));

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;
        int total = EXPECTED_OUTPUTS.size();

        System.out.println("⚡ Running ThunderJS TestRunner...");
        System.out.println("=================================");

        for (Map.Entry<String, String> entry : EXPECTED_OUTPUTS.entrySet()) {
            String filepath = entry.getKey();
            String expected = entry.getValue().replace("\r\n", "\n");

            System.out.printf("Test: %-30s ... ", filepath);

            try {
                String source = Files.readString(Path.of(filepath));

                // Capture standard output
                PrintStream originalOut = System.out;
                ByteArrayOutputStream outContent = new ByteArrayOutputStream();
                System.setOut(new PrintStream(outContent));

                try {
                    Lexer lexer = new Lexer(source);
                    List<Token> tokens = lexer.tokenize();
                    Parser parser = new Parser(tokens);
                    List<Stmt> statements = parser.parse();
                    Interpreter interpreter = new Interpreter();
                    interpreter.interpret(statements);
                } finally {
                    System.setOut(originalOut);
                }

                String actual = outContent.toString().replace("\r\n", "\n");
                if (actual.equals(expected)) {
                    System.out.println("PASSED ✅");
                    passed++;
                } else {
                    System.out.println("FAILED ❌");
                    System.out.println("  Expected:");
                    System.out.print(indent(expected));
                    System.out.println("  Actual:");
                    System.out.print(indent(actual));
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("ERROR 💥");
                e.printStackTrace();
                failed++;
            }
        }

        System.out.println("=================================");
        System.out.printf("Results: %d / %d Passed (%d Failed)\n", passed, total, failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    private static String indent(String text) {
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n")) {
            sb.append("    ").append(line).append("\n");
        }
        return sb.toString();
    }
}
