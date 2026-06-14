package thunderjs;

import thunderjs.ast.Stmt;
import thunderjs.interpreter.Interpreter;
import thunderjs.lexer.Lexer;
import thunderjs.lexer.Token;
import thunderjs.parser.Parser;
import thunderjs.runtime.RuntimeError;
import thunderjs.runtime.Diagnostic;
import thunderjs.runtime.DiagnosticFormatter;
import thunderjs.runtime.StackFrame;

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
            if (arg.equals("--ast")) {
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
            } else if (arg.equals("--repl")) {
                options.repl = true;
            } else if (arg.equals("--format")) {
                options.format = true;
            } else if (arg.equals("--minify")) {
                options.minify = true;
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
        boolean isFile = false;
        String suggestion = null;

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            isFile = true;
        } else {
            // Check if it was likely intended as a file
            if (sourceArg.endsWith(".js")) {
                isFile = true;
            } else if (Files.exists(Path.of(sourceArg + ".js"))) {
                isFile = true;
                suggestion = sourceArg + ".js";
            } else {
                Path parent = filePath.getParent();
                String prefix = "";
                if (parent != null) {
                    prefix = parent.toString() + "/";
                } else {
                    parent = Path.of(".");
                }
                if (Files.exists(parent) && Files.isDirectory(parent)) {
                    String searchName = filePath.getFileName().toString();
                    try (java.util.stream.Stream<Path> stream = Files.list(parent)) {
                        List<Path> paths = stream.toList();
                        String bestMatch = null;
                        int minDistance = Integer.MAX_VALUE;
                        for (Path p : paths) {
                            if (Files.isRegularFile(p)) {
                                String fName = p.getFileName().toString();
                                if (fName.endsWith(".js")) {
                                    String fNameNoExt = fName.substring(0, fName.length() - 3);
                                    
                                    // check exact matches
                                    if (fName.equalsIgnoreCase(searchName) || fNameNoExt.equalsIgnoreCase(searchName)) {
                                        isFile = true;
                                        bestMatch = fName;
                                        minDistance = 0;
                                        break;
                                    }
                                    
                                    // check distance to full name
                                    int dist1 = thunderjs.util.Levenshtein.distance(searchName, fName);
                                    if (dist1 < minDistance) {
                                        minDistance = dist1;
                                        bestMatch = fName;
                                    }
                                    // check distance to name without extension
                                    int dist2 = thunderjs.util.Levenshtein.distance(searchName, fNameNoExt);
                                    if (dist2 < minDistance) {
                                        minDistance = dist2;
                                        bestMatch = fName;
                                    }
                                }
                            }
                        }
                        if (minDistance <= 3 && bestMatch != null) {
                            isFile = true;
                            suggestion = prefix + bestMatch;
                        }
                    } catch (IOException ignored) {}
                }
            }
        }

        if (isFile && !Files.exists(filePath)) {
            System.err.println("\u001B[31mError: File not found: " + sourceArg + "\u001B[0m");
            if (suggestion != null) {
                System.err.println("\n  💡 \u001B[32mDid you mean: " + suggestion + "\u001B[0m");
            }
            System.exit(1);
            return;
        }

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

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            if (options.showAst) {
                features.ASTPrinter astPrinter = new features.ASTPrinter();
                System.out.print(astPrinter.print(statements));
                if (!options.benchmark) return;
            }

            if (options.format) {
                features.Formatter formatter = new features.Formatter();
                System.out.println(formatter.format(statements));
                if (!options.benchmark) return;
            }

            if (options.minify) {
                features.Minifier minifier = new features.Minifier();
                System.out.println(minifier.minify(statements));
                if (!options.benchmark) return;
            }

            if (options.showAst || options.format || options.minify) {
                if (!options.benchmark) return;
            }

            Interpreter interpreter = new Interpreter();
            interpreter.setSourceContext(source, sourceArg);

            features.CoverageTracker coverageTracker = new features.CoverageTracker(options.coverage);
            features.TraceEngine traceEngine = new features.TraceEngine(options.trace);
            features.ExplainEngine explainEngine = new features.ExplainEngine(options.explain);

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

        } catch (Lexer.LexerError | Parser.ParseError | RuntimeError e) {
            handleAndPrintDiagnostics(e, source, sourceArg);
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("""
            ⚡ Tode — A Java-based JavaScript Runtime
            
            Usage: tode [flags] <file.js | 'code'>
            
            Flags:
              --ast              Print the parsed AST in Unicode tree layout
              --bench            Print execution time after running
              --trace            Show step-by-step execution trace
              --explain          Show human-readable execution narration
              --explain <range>  Only explain specified line range, e.g. 10-20
              --explain-lines    Same as --explain <range>
              --coverage         Show which JS features were used
              --format           Pretty-print / auto-format the source code
              --minify           Output minified (compressed) source code
              --repl             Start the interactive REPL
              --help, -h         Show this help message
              --version          Show version
            
            Examples:
              tode test.js
              tode --explain 3-8 test.js
              tode --ast test.js
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

        features.CoverageTracker coverageTracker = new features.CoverageTracker(options.coverage);
        features.TraceEngine traceEngine = new features.TraceEngine(options.trace);
        features.ExplainEngine explainEngine = new features.ExplainEngine(options.explain);

        interpreter.setCoverageTracker(coverageTracker);
        interpreter.setTraceEngine(traceEngine);
        interpreter.setExplainEngine(explainEngine);

        try {
            while (true) {
                System.out.print("> ");
                String line = null;
                try {
                    line = reader.readLine();
                    if (line == null || line.trim().equals("exit()")) {
                        break;
                    }
                    if (line.trim().isEmpty()) continue;

                    Lexer lexer = new Lexer(line);
                    List<Token> tokens = lexer.tokenize();

                    Parser parser = new Parser(tokens);
                    List<Stmt> statements = parser.parse();

                    if (options.showAst) {
                        features.ASTPrinter astPrinter = new features.ASTPrinter();
                        System.out.print(astPrinter.print(statements));
                    }

                    interpreter.setSourceContext(line, "repl");
                    interpreter.interpret(statements);
                } catch (Lexer.LexerError | Parser.ParseError | RuntimeError e) {
                    handleAndPrintDiagnostics(e, line, "repl");
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

    private static void handleAndPrintDiagnostics(Throwable e, String source, String sourceArg) {
        String errorType = "RuntimeError";
        String msg = e.getMessage();
        int line = 1;
        int col = 1;
        int caretLength = 1;
        String suggestion = null;
        
        if (e instanceof Lexer.LexerError) {
            errorType = "SyntaxError";
            Lexer.LexerError le = (Lexer.LexerError) e;
            line = le.getLine();
            col = le.getColumn();
        } else if (e instanceof Parser.ParseError) {
            errorType = "SyntaxError";
            Parser.ParseError pe = (Parser.ParseError) e;
            line = pe.getLine();
            col = pe.getColumn();
            if (pe.getLexeme() != null) {
                caretLength = pe.getLexeme().length();
            }
        }
        java.util.List<StackFrame> callStack = null;
        if (e instanceof RuntimeError) {
            RuntimeError re = (RuntimeError) e;
            line = re.getLine();
            col = re.getColumn();
            suggestion = re.getSuggestion();
            callStack = re.getCallStack();
            Token tok = re.getToken();
            if (tok != null && tok.getLexeme() != null) {
                caretLength = tok.getLexeme().length();
            }
            if (msg != null) {
                if (msg.startsWith("TypeError: ")) {
                    errorType = "TypeError";
                    msg = msg.substring(11);
                } else if (msg.startsWith("ReferenceError: ")) {
                    errorType = "ReferenceError";
                    msg = msg.substring(16);
                } else if (msg.startsWith("RangeError: ")) {
                    errorType = "RangeError";
                    msg = msg.substring(12);
                }
            }
        }
        
        Diagnostic diag = Diagnostic.error(errorType, msg)
            .at(sourceArg, line, col)
            .withSource(source)
            .withCaretLength(caretLength)
            .withSuggestion(suggestion)
            .withCallStack(callStack)
            .build();
            
        System.err.print(DiagnosticFormatter.format(diag));
    }

    static class RunOptions {
        boolean showAst = false;
        boolean benchmark = false;
        boolean trace = false;
        boolean explain = false;
        boolean coverage = false;
        boolean repl = false;
        boolean format = false;
        boolean minify = false;
        String explainRange = null;
    }
}
