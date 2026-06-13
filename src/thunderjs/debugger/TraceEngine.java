package thunderjs.debugger;

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
        System.out.printf("[Step %d]\n", stepCount);
        System.out.println("Execute:");
        System.out.println(ASTPrinter.SourceCodeReconstructor.toSource(stmt));
        lastValue = null;
        lastConsoleOutput = null;
    }

    public void afterExecute(Stmt stmt, Environment env) {
        if (!enabled) return;
        if (lastConsoleOutput != null) {
            System.out.println();
            System.out.println("Output:");
            System.out.println(lastConsoleOutput);
            lastConsoleOutput = null;
        } else if (lastValue != null && (stmt instanceof Stmt.VarDeclaration || stmt instanceof Stmt.ExpressionStmt)) {
            System.out.println();
            System.out.println("Result:");
            System.out.println(Stringify.stringify(lastValue));
        }
        System.out.println();
        System.out.println("Environment:");
        System.out.println(formatEnvironment(collectVariables(env)));
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
                    name.equals("console") || name.equals("Math") || name.equals("Object")) {
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

    private String formatEnvironment(Map<String, Object> vars) {
        if (vars.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        int count = 0;
        for (Map.Entry<String, Object> entry : vars.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(Stringify.stringify(entry.getValue()));
            if (++count < vars.size()) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
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
            System.err.println(errorMessage);
            return;
        }
        System.err.println("Uncaught RuntimeError: " + errorMessage);
        for (int i = frames.size() - 1; i >= 0; i--) {
            Frame frame = frames.get(i);
            System.err.printf("    at %s (line %d)\n", frame.name, frame.line);
        }
    }
}
