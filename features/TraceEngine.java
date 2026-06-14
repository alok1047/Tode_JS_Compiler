package features;

import thunderjs.ast.Stmt;
import thunderjs.runtime.Environment;
import thunderjs.builtins.ConsoleObject;
import thunderjs.util.Stringify;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TraceEngine {
    public static class Frame {
        public final String name;
        public final int line;

        public Frame(String name, int line) {
            this.name = name;
            this.line = line;
        }
    }

    private final List<Frame> frames = new ArrayList<>();
    private final boolean enabled;
    private int stepCount = 0;
    private Object lastValue = null;
    private String lastConsoleOutput = null;

    public TraceEngine(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            ConsoleObject.listener = text -> {
                lastConsoleOutput = text;
            };
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setLastValue(Object val) {
        if (!enabled) return;
        this.lastValue = val;
    }

    public void beforeExecute(Stmt stmt, Environment env) {
        if (!enabled) return;
        if (stepCount == 0) {
            System.out.println("⚡ ThunderJS Execution Trace");
        }
        stepCount++;
        System.out.println();
        System.out.printf("▶ Step %d\n", stepCount);
        System.out.println("  Executing statement:");
        String source = ASTPrinter.SourceCodeReconstructor.toSource(stmt);
        for (String line : source.split("\n")) {
            System.out.println("    " + line);
        }
        lastValue = null;
        lastConsoleOutput = null;
    }

    public void afterExecute(Stmt stmt, Environment env) {
        if (!enabled) return;
        if (lastConsoleOutput != null) {
            System.out.println();
            System.out.println("  Console Output:");
            for (String line : lastConsoleOutput.split("\n")) {
                System.out.println("    " + line);
            }
            lastConsoleOutput = null;
        } else if (lastValue != null && (stmt instanceof Stmt.VarDeclaration || stmt instanceof Stmt.ExpressionStmt)) {
            System.out.println();
            System.out.println("  Result of statement:");
            System.out.println("    " + Stringify.stringify(lastValue));
        }
        
        System.out.println();
        System.out.println("  Active Variables:");
        Map<String, Object> vars = collectVariables(env);
        if (vars.isEmpty()) {
            System.out.println("    (No active variables in scope)");
        } else {
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                System.out.println("    • " + entry.getKey() + " = " + Stringify.stringify(entry.getValue()));
            }
        }
    }

    private Map<String, Object> collectVariables(Environment env) {
        Map<String, Object> allVars = new TreeMap<>();
        Environment current = env;
        while (current != null) {
            Map<String, Object> local = current.getLocalValues();
            for (Map.Entry<String, Object> entry : local.entrySet()) {
                String name = entry.getKey();
                if (name.equals("NaN") || name.equals("Infinity") || name.equals("undefined") ||
                    name.equals("String") || name.equals("Number") || name.equals("Boolean") ||
                    name.equals("parseInt") || name.equals("parseFloat") || name.equals("isNaN") ||
                    name.equals("console") || name.equals("Math") || name.equals("Object") ||
                    name.equals("Date")) {
                    continue;
                }
                if (!allVars.containsKey(name)) {
                    allVars.put(name, entry.getValue());
                }
            }
            current = current.getParent();
        }
        return allVars;
    }

    public void beforeCall(String name, List<Object> args, List<String> paramNames) {
        if (!enabled) return;
        System.out.println();
        System.out.println("▶ Function Call:");
        System.out.printf("  Calling '%s' with arguments:\n", name != null ? name : "anonymous");
        if (args.isEmpty()) {
            System.out.println("    (No arguments passed)");
        } else {
            for (int i = 0; i < args.size(); i++) {
                String paramName = (paramNames != null && i < paramNames.size()) ? paramNames.get(i) : "arg" + i;
                System.out.println("    • " + paramName + " = " + Stringify.stringify(args.get(i)));
            }
        }
    }

    public void afterCall(String name, Object result) {
        if (!enabled) return;
        System.out.println();
        System.out.println("◀ Function Return:");
        System.out.printf("  Returned from '%s' with value:\n", name != null ? name : "anonymous");
        System.out.println("    " + Stringify.stringify(result));
    }



    // ── Call Stack Tracing (Backward Compatibility & Errors) ─────────────────

    public void push(String name, int line) {
        if (!enabled) return;
        frames.add(new Frame(name, line));
    }

    public void pop() {
        if (!enabled) return;
        if (!frames.isEmpty()) {
            frames.remove(frames.size() - 1);
        }
    }

    public void printStackTrace(String errorMessage) {
        if (!enabled) {
            return;
        }
        System.err.println("Uncaught RuntimeError: " + errorMessage);
        for (int i = frames.size() - 1; i >= 0; i--) {
            Frame frame = frames.get(i);
            System.err.printf("    at %s (line %d)\n", frame.name, frame.line);
        }
    }
}
