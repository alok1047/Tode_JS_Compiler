package thunderjs.debugger;

import thunderjs.ast.Expr;
import thunderjs.ast.Stmt;
import thunderjs.lexer.Token;
import thunderjs.util.Stringify;

public class ExplainEngine {
    private final boolean enabled;
    private int explainStart = -1;
    private int explainEnd = -1;
    private int lastPrintedLine = -1;
    private boolean headerPrinted = false;
    private boolean lastActionWasCalculation = false;

    public ExplainEngine(boolean enabled) {
        this.enabled = enabled;
        if (enabled && thunderjs.builtins.ConsoleObject.listener == null) {
            thunderjs.builtins.ConsoleObject.listener = text -> {};
        }
    }

    public void setLineRange(int start, int end) {
        this.explainStart = start;
        this.explainEnd = end;
    }

    private boolean shouldExplain(int line) {
        if (!enabled) return false;
        if (explainStart != -1 && explainEnd != -1) {
            return line >= explainStart && line <= explainEnd;
        }
        return true;
    }

    public void printHeader() {
        if (!enabled) return;
        if (explainStart != -1 && explainEnd != -1) {
            System.out.println("🧠 Focused Explain Mode");
            System.out.println();
            System.out.printf("Lines: %d-%d\n", explainStart, explainEnd);
        } else {
            System.out.println("🧠 ThunderJS Explain Mode");
        }
        headerPrinted = true;
    }

    private void printLineHeader(int line) {
        if (!headerPrinted) {
            printHeader();
        }
        if (line != lastPrintedLine) {
            System.out.println();
            System.out.printf("[Line %d]\n", line);
            lastPrintedLine = line;
        }
    }

    public void explainVarDecl(Token name, Object val, int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        if (lastActionWasCalculation) {
            System.out.println("Store:");
        } else {
            System.out.println("Create variable:");
        }
        System.out.printf("%s = %s\n", name.getLexeme(), Stringify.stringify(val));
        lastActionWasCalculation = false;
    }

    public void explainAssign(Token name, Object val, int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        if (lastActionWasCalculation) {
            System.out.println("Store:");
        } else {
            System.out.println("Assign variable:");
        }
        System.out.printf("%s = %s\n", name.getLexeme(), Stringify.stringify(val));
        lastActionWasCalculation = false;
    }

    public void explainIf(Expr condition, boolean branch, int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Evaluate if condition:");
        System.out.println(ASTPrinter.SourceCodeReconstructor.toSource(condition));
        System.out.println();
        System.out.println("Result:");
        System.out.println(branch ? "True (then branch taken)" : "False (else branch taken)");
        lastActionWasCalculation = false;
    }

    public void explainFor(int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Loop iteration (for)");
        lastActionWasCalculation = false;
    }

    public void explainWhile(int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Loop iteration (while)");
        lastActionWasCalculation = false;
    }

    public void explainDoWhile(int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Loop iteration (do...while)");
        lastActionWasCalculation = false;
    }

    public void explainFunctionDecl(Token name, int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Declare function:");
        System.out.println(name.getLexeme());
        lastActionWasCalculation = false;
    }

    public void explainCall(String calleeName, int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Call function:");
        System.out.println(calleeName);
        lastActionWasCalculation = false;
    }

    public void explainPrint(String output, int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Print:");
        System.out.println(output);
        lastActionWasCalculation = false;
    }

    public void explainReturn(Object val, int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Return:");
        System.out.println(Stringify.stringify(val));
        lastActionWasCalculation = false;
    }

    public void explainSwitch(Object disc, int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Switch discriminant:");
        System.out.println(Stringify.stringify(disc));
        lastActionWasCalculation = false;
    }

    public void explainBinary(Expr.Binary expr, Object result, int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Calculate:");
        System.out.println(ASTPrinter.SourceCodeReconstructor.toSource(expr));
        System.out.println();
        System.out.println("Result:");
        System.out.println(Stringify.stringify(result));
        lastActionWasCalculation = true;
    }

    public void explainUnary(Expr.Unary expr, Object result, int line) {
        if (!shouldExplain(line)) return;
        printLineHeader(line);
        System.out.println("Calculate:");
        System.out.println(ASTPrinter.SourceCodeReconstructor.toSource(expr));
        System.out.println();
        System.out.println("Result:");
        System.out.println(Stringify.stringify(result));
        lastActionWasCalculation = true;
    }
}
