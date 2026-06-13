package thunderjs;

import thunderjs.ast.Stmt;
import thunderjs.interpreter.Interpreter;
import thunderjs.lexer.Lexer;
import thunderjs.lexer.Token;
import thunderjs.parser.Parser;
import thunderjs.runtime.RuntimeError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            if (System.console() != null) {
                runRepl(new RunOptions());
            } else {
                try {
                    String source = new String(System.in.readAllBytes());
                    run(source, new RunOptions(), null);
                } catch (IOException e) {
                    System.err.println("Error reading stdin: " + e.getMessage());
                    System.exit(1);
                }
            }
            return;
        }

        RunOptions options = new RunOptions();
        String sourceArg = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--tokens")) {
                options.showTokens = true;
            } else if (arg.equals("--ast")) {
                options.showAst = true;
            } else if (arg.equals("--bench") || arg.equals("--time")) {
                options.benchmark = true;
            } else if (arg.equals("--trace")) {
                options.trace = true;
            } else if (arg.equals("--explain")) {
                options.explain = true;
                if (i + 1 < args.length && args[i + 1].matches("\\d+-\\d+")) {
                    options.explainRange = args[i + 1];
                    i++;
                }
            } else if (arg.equals("--explain-lines")) {
                options.explain = true;
                if (i + 1 < args.length) {
                    options.explainRange = args[i + 1];
                    i++;
                }
            } else if (arg.startsWith("--explain-lines=")) {
                options.explain = true;
                options.explainRange = arg.substring("--explain-lines=".length());
            } else if (arg.equals("--coverage")) {
                options.coverage = true;
            } else if (arg.equals("--debug")) {
                options.debug = true;
            } else if (arg.equals("--repl")) {
                options.repl = true;
            } else if (arg.equals("--visual")) {
                options.visual = true;
            } else if (arg.equals("--html")) {
                options.html = true;
            } else if (arg.equals("--quiet")) {
                options.quiet = true;
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printHelp();
                return;
            } else if (arg.equals("--version") || arg.equals("-v")) {
                printVersion();
                return;
            } else {
                if (arg.startsWith("-")) {
                    System.err.println("Unknown flag: " + arg);
                    System.exit(1);
                }
                sourceArg = arg;
            }
        }

        if (options.repl) {
            runRepl(options);
            return;
        }

        if (sourceArg == null) {
            System.err.println("Usage: tode [flags] <file.js | 'code'>");
            System.err.println("Run --help for more information.");
            System.exit(1);
            return;
        }

        String source;
        Path filePath = Path.of(sourceArg);
        if (Files.exists(filePath)) {
            try {
                source = Files.readString(filePath);
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                System.exit(1);
                return;
            }
        } else {
            source = sourceArg;
        }

        run(source, options, sourceArg);
    }

    private static void run(String source, RunOptions options, String sourceArg) {


        try {
            long startTime = System.nanoTime();

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();

            if (options.showTokens) {
                System.out.println("=== Token Stream ===");
                for (Token token : tokens) {
                    System.out.println(token);
                }
                if (!options.showAst && !options.benchmark && !options.visual && !options.html) return;
            }

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            if (options.showAst) {
                thunderjs.debugger.ASTPrinter astPrinter = new thunderjs.debugger.ASTPrinter();
                System.out.print(astPrinter.print(statements));
                if (options.html) {
                    generateHTML(statements, sourceArg);
                }
                if (!options.benchmark) return;
            }

            if (options.visual) {
                System.out.print(thunderjs.debugger.VisualTreeGenerator.generate(statements));
                if (options.html) {
                    generateHTML(statements, sourceArg);
                }
                if (!options.benchmark) return;
            } else if (options.html) {
                generateHTML(statements, sourceArg);
                if (!options.benchmark) return;
            }

            if (options.showAst || options.visual || options.html) {
                if (!options.benchmark) return;
            }

            Interpreter interpreter = new Interpreter();

            thunderjs.debugger.CoverageTracker coverageTracker = new thunderjs.debugger.CoverageTracker(options.coverage);
            thunderjs.debugger.TraceEngine traceEngine = new thunderjs.debugger.TraceEngine(options.trace);
            thunderjs.debugger.ExplainEngine explainEngine = new thunderjs.debugger.ExplainEngine(options.explain);

            if (options.explain && options.explainRange != null) {
                String[] parts = options.explainRange.split("-");
                if (parts.length == 2) {
                    try {
                        int start = Integer.parseInt(parts[0]);
                        int end = Integer.parseInt(parts[1]);
                        explainEngine.setLineRange(start, end);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid explain range: " + options.explainRange);
                    }
                }
            }

            interpreter.setCoverageTracker(coverageTracker);
            interpreter.setTraceEngine(traceEngine);
            interpreter.setExplainEngine(explainEngine);

            try {
                interpreter.interpret(statements);
            } finally {
                thunderjs.builtins.ConsoleObject.listener = null;
            }

            if (options.coverage) {
                coverageTracker.printSummary();
            }

            long endTime = System.nanoTime();

            if (options.benchmark) {
                double ms = (endTime - startTime) / 1_000_000.0;
                System.err.printf("%n⚡ Execution time: %.3f ms%n", ms);
            }

        } catch (Lexer.LexerError e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Parser.ParseError e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (RuntimeError e) {
            System.err.println(e.toString());
            System.exit(1);
        }
    }

    private static void generateHTML(List<Stmt> statements, String sourceArg) {
        String baseName = "inline";
        if (sourceArg != null) {
            java.nio.file.Path p = java.nio.file.Path.of(sourceArg);
            if (java.nio.file.Files.exists(p)) {
                String filename = p.getFileName().toString();
                int dotIdx = filename.lastIndexOf('.');
                if (dotIdx > 0) {
                    baseName = filename.substring(0, dotIdx);
                } else {
                    baseName = filename;
                }
            }
        }
        try {
            java.nio.file.Path visDir = java.nio.file.Path.of("visualizations");
            java.nio.file.Files.createDirectories(visDir);
            java.nio.file.Path visFile = visDir.resolve(baseName + ".html");

            thunderjs.debugger.ASTPrinter astPrinter = new thunderjs.debugger.ASTPrinter();
            thunderjs.debugger.ASTPrinter.TreeNode root = astPrinter.getRootNode(statements);

            String htmlContent = thunderjs.debugger.HTMLVisualizer.generate(root);
            java.nio.file.Files.writeString(visFile, htmlContent);
            System.out.println("✓ HTML visualization created:");
            System.out.println(visFile.toString());
        } catch (java.io.IOException e) {
            System.err.println("Error generating HTML: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("""
            ⚡ Tode — A Java-based JavaScript Runtime
            
            Usage: tode [flags] <file.js | 'code'>
            
            Flags:
              --tokens           Print the token stream from the lexer
              --ast              Print the parsed AST in Unicode tree layout
              --bench            Print execution time after running
              --trace            Show step-by-step execution trace
              --explain          Show human-readable execution narration
              --explain <range>  Only explain specified line range, e.g. 10-20
              --explain-lines    Same as --explain <range>
              --visual           Print category-based visual tree
              --html             Generate collapsible AST tree HTML visualization
              --coverage         Show which JS features were used
              --debug            Show variable state during execution
              --quiet            Suppress the startup banner
              --help, -h         Show this help message
              --version          Show version
            
            Examples:
              tode test.js
              tode --explain 3-8 test.js
              tode --visual test.js --html
            """);
    }

    private static void printVersion() {
        System.out.println("Tode v1.0.0 — Powered by ThunderJS");
    }

    private static void runRepl(RunOptions options) {
        System.out.println("⚡ Tode Interactive REPL (v1.0.0)");
        System.out.println("Type exit() or press Ctrl+C to quit.\n");

        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        Interpreter interpreter = new Interpreter();

        thunderjs.debugger.CoverageTracker coverageTracker = new thunderjs.debugger.CoverageTracker(options.coverage);
        thunderjs.debugger.TraceEngine traceEngine = new thunderjs.debugger.TraceEngine(options.trace);
        thunderjs.debugger.ExplainEngine explainEngine = new thunderjs.debugger.ExplainEngine(options.explain);

        interpreter.setCoverageTracker(coverageTracker);
        interpreter.setTraceEngine(traceEngine);
        interpreter.setExplainEngine(explainEngine);

        try {
            while (true) {
                System.out.print("> ");
                try {
                    String line = reader.readLine();
                    if (line == null || line.trim().equals("exit()")) {
                        break;
                    }
                    if (line.trim().isEmpty()) continue;

                    Lexer lexer = new Lexer(line);
                    List<Token> tokens = lexer.tokenize();

                    if (options.showTokens) {
                        System.out.println("=== Token Stream ===");
                        for (Token token : tokens) System.out.println(token);
                    }

                    Parser parser = new Parser(tokens);
                    List<Stmt> statements = parser.parse();

                    if (options.showAst) {
                        thunderjs.debugger.ASTPrinter astPrinter = new thunderjs.debugger.ASTPrinter();
                        System.out.print(astPrinter.print(statements));
                    }

                    interpreter.interpret(statements);
                } catch (Lexer.LexerError | Parser.ParseError | RuntimeError e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("REPL IO Error: " + e.getMessage());
        } finally {
            thunderjs.builtins.ConsoleObject.listener = null;
        }

        if (options.coverage) {
            coverageTracker.printSummary();
        }
    }

    static class RunOptions {
        boolean showTokens = false;
        boolean showAst = false;
        boolean benchmark = false;
        boolean trace = false;
        boolean explain = false;
        boolean coverage = false;
        boolean debug = false;
        boolean repl = false;
        boolean visual = false;
        boolean html = false;
        boolean quiet = false;
        String explainRange = null;
    }
}
