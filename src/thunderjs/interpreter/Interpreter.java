package thunderjs.interpreter;

import thunderjs.ast.Expr;
import thunderjs.ast.Stmt;
import thunderjs.builtins.ConsoleObject;
import thunderjs.builtins.MathObject;
import thunderjs.lexer.Token;
import thunderjs.lexer.TokenType;
import thunderjs.runtime.*;
import thunderjs.util.Stringify;
import thunderjs.util.SuggestionEngine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * Tree-walking interpreter for ThunderJS.
 *
 * Implements both {@link Expr.Visitor} and {@link Stmt.Visitor} to
 * evaluate all AST nodes. Manages the runtime environment chain and
 * dispatches calls to built-in objects (console, Math, Array, String).
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    /** The global environment. */
    private final Environment globals = new Environment();

    /** The current environment (changes with scope entry/exit). */
    private Environment environment = globals;

    // Debugger engines
    private features.CoverageTracker coverageTracker = new features.CoverageTracker(false);
    private features.TraceEngine traceEngine = new features.TraceEngine(false);
    private features.ExplainEngine explainEngine = new features.ExplainEngine(false);

    public void setCoverageTracker(features.CoverageTracker tracker) { this.coverageTracker = tracker; }
    public void setTraceEngine(features.TraceEngine engine) { this.traceEngine = engine; }
    public void setExplainEngine(features.ExplainEngine engine) { this.explainEngine = engine; }

    // Source context fields
    private String sourceCode = "";
    private String fileName = "input.js";

    public void setSourceContext(String source, String fileName) {
        this.sourceCode = source;
        this.fileName = fileName != null ? fileName : "input.js";
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getFileName() {
        return fileName;
    }

    // Call stack for runtime errors
    private final List<StackFrame> callStack = new ArrayList<>();

    public List<StackFrame> getCallStack() {
        return new ArrayList<>(callStack);
    }

    // ── Signal exceptions for control flow ──────────────────────────────

    /** Thrown to implement 'return' as a non-local jump. */
    public static class ReturnSignal extends RuntimeException {
        public final Object value;
        public ReturnSignal(Object value) {
            super(null, null, true, false);
            this.value = value;
        }
    }

    /** Thrown to implement 'break' as a non-local jump. */
    public static class BreakSignal extends RuntimeException {
        public BreakSignal() { super(null, null, true, false); }
    }

    /** Thrown to implement 'continue' as a non-local jump. */
    public static class ContinueSignal extends RuntimeException {
        public ContinueSignal() { super(null, null, true, false); }
    }

    // ── Constructor ─────────────────────────────────────────────────────

    public Interpreter() {
        defineGlobals();
    }

    private void defineGlobals() {
        globals.define("NaN", Double.NaN);
        globals.define("Infinity", Double.POSITIVE_INFINITY);
        globals.define("undefined", JSUndefined.INSTANCE);

        globals.define("String", (JSCallable) (interp, args) -> {
            if (args.isEmpty()) return "";
            return Stringify.toJSString(args.get(0));
        });
        globals.define("Number", (JSCallable) (interp, args) -> {
            if (args.isEmpty()) return 0.0;
            return Stringify.toJSNumber(args.get(0));
        });
        globals.define("Boolean", (JSCallable) (interp, args) -> {
            if (args.isEmpty()) return false;
            return Stringify.isTruthy(args.get(0));
        });

        globals.define("parseInt", (JSCallable) (interp, args) -> {
            if (args.isEmpty()) return Double.NaN;
            String s = Stringify.toJSString(args.get(0)).trim();
            try {
                int radix = 10;
                if (args.size() > 1) radix = (int) Stringify.toJSNumber(args.get(1));
                return (double) Integer.parseInt(s, radix);
            } catch (NumberFormatException e) {
                StringBuilder num = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (i == 0 && (c == '-' || c == '+')) { num.append(c); continue; }
                    if (c >= '0' && c <= '9') num.append(c); else break;
                }
                if (num.length() == 0 || (num.length() == 1 && (num.charAt(0) == '-' || num.charAt(0) == '+')))
                    return Double.NaN;
                try { return (double) Integer.parseInt(num.toString()); }
                catch (Exception ex) { return Double.NaN; }
            }
        });

        globals.define("parseFloat", (JSCallable) (interp, args) -> {
            if (args.isEmpty()) return Double.NaN;
            return Stringify.toJSNumber(args.get(0));
        });

        globals.define("isNaN", (JSCallable) (interp, args) -> {
            if (args.isEmpty()) return true;
            return Double.isNaN(Stringify.toJSNumber(args.get(0)));
        });

        globals.define("isFinite", (JSCallable) (interp, args) -> {
            if (args.isEmpty()) return false;
            double num = Stringify.toJSNumber(args.get(0));
            return !Double.isNaN(num) && !Double.isInfinite(num);
        });

        LinkedHashMap<String, Object> consoleObj = new LinkedHashMap<>();
        consoleObj.put("log", (JSCallable) (interp, args) -> {
            ConsoleObject.log(args);
            return JSUndefined.INSTANCE;
        });
        globals.define("console", consoleObj);

        LinkedHashMap<String, Object> mathObj = new LinkedHashMap<>();
        for (String key : List.of("PI", "E", "floor", "ceil", "round", "abs", "sqrt", "pow", "random", "max", "min")) {
            mathObj.put(key, getMathProperty(key));
        }
        globals.define("Math", mathObj);

        LinkedHashMap<String, Object> objectObj = new LinkedHashMap<>();
        for (String key : List.of("keys", "values", "entries", "getOwnPropertySymbols")) {
            objectObj.put(key, getObjectStaticMethod(key));
        }
        globals.define("Object", objectObj);
        globals.define("Date", new DateConstructor());
        globals.define("Set", new SetConstructor());
        globals.define("Map", new MapConstructor());
        globals.define("Symbol", (JSCallable) (interp, args) -> {
            String desc = args.isEmpty() ? null : Stringify.toJSString(args.get(0));
            return new JSSymbol(desc);
        });
    }

    // ── Public API ──────────────────────────────────────────────────────

    public void interpret(List<Stmt> statements) {
        hoistFunctions(statements, this.environment);
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } catch (RuntimeError e) {
            if (e.getCallStack() == null) {
                if (!callStack.isEmpty()) {
                    StackFrame top = callStack.get(callStack.size() - 1);
                    top.line = e.getLine();
                    top.column = e.getColumn();
                }
                e.setCallStack(new ArrayList<>(callStack));
            }
            traceEngine.printStackTrace(e.getMessage());
            throw e;
        }
    }

    public Environment getGlobals() { return globals; }

    // ── Internal dispatch ───────────────────────────────────────────────

    private void execute(Stmt stmt) {
        if (stmt != null) {
            coverageTracker.markVisited(stmt);
            if (traceEngine != null && traceEngine.isEnabled() && !(stmt instanceof Stmt.Block)) {
                traceEngine.beforeExecute(stmt, environment);
            }
            stmt.accept(this);
            if (traceEngine != null && traceEngine.isEnabled() && !(stmt instanceof Stmt.Block)) {
                traceEngine.afterExecute(stmt, environment);
            }
        }
    }

    public void executeBlock(List<Stmt> statements, Environment env) {
        hoistFunctions(statements, env);
        Environment previous = this.environment;
        try {
            this.environment = env;
            for (Stmt stmt : statements) execute(stmt);
        } finally {
            this.environment = previous;
        }
    }

    private void hoistFunctions(List<Stmt> statements, Environment env) {
        if (statements == null) return;
        for (Stmt stmt : statements) {
            if (stmt instanceof Stmt.FunctionDecl fnDecl) {
                JSFunction fn = new JSFunction(fnDecl.name.getLexeme(), fnDecl.params, fnDecl.body, env);
                env.define(fnDecl.name.getLexeme(), fn);
            }
        }
    }

    private Object evaluate(Expr expr) {
        if (expr != null) {
            coverageTracker.markVisited(expr);
            Object val = expr.accept(this);
            if (traceEngine != null && traceEngine.isEnabled()) {
                traceEngine.setLastValue(val);
            }
            return val;
        }
        return JSUndefined.INSTANCE;
    }

    // ════════════════════════════════════════════════════════════════════
    //  STATEMENT VISITORS
    // ════════════════════════════════════════════════════════════════════

    @Override
    public Void visitExpressionStmt(Stmt.ExpressionStmt stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarDeclarationStmt(Stmt.VarDeclaration stmt) {
        Object value = JSUndefined.INSTANCE;
        if (stmt.initializer != null) value = evaluate(stmt.initializer);
        boolean isConst = stmt.keyword.getType() == TokenType.CONST;
        environment.define(stmt.name.getLexeme(), value, isConst);
        explainEngine.explainVarDecl(stmt.name, value, getLine(stmt));
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Object conditionVal = evaluate(stmt.condition);
        boolean cond = Stringify.isTruthy(conditionVal);
        explainEngine.explainIf(stmt.condition, cond, getLine(stmt));
        if (cond) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        explainEngine.explainFor(getLine(stmt));
        Environment forEnv = new Environment(environment);
        Environment previous = this.environment;
        try {
            this.environment = forEnv;
            if (stmt.initializer != null) execute(stmt.initializer);
            while (true) {
                if (stmt.condition != null && !Stringify.isTruthy(evaluate(stmt.condition))) break;
                try { execute(stmt.body); }
                catch (BreakSignal e) { break; }
                catch (ContinueSignal e) { /* fall through to increment */ }
                if (stmt.increment != null) evaluate(stmt.increment);
            }
        } finally { this.environment = previous; }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (true) {
            Object condVal = evaluate(stmt.condition);
            if (!Stringify.isTruthy(condVal)) break;
            explainEngine.explainWhile(getLine(stmt));
            try { execute(stmt.body); }
            catch (BreakSignal e) { break; }
            catch (ContinueSignal e) { /* continue */ }
        }
        return null;
    }

    @Override
    public Void visitDoWhileStmt(Stmt.DoWhile stmt) {
        do {
            explainEngine.explainDoWhile(getLine(stmt));
            try { execute(stmt.body); }
            catch (BreakSignal e) { break; }
            catch (ContinueSignal e) { /* continue */ }
        } while (Stringify.isTruthy(evaluate(stmt.condition)));
        return null;
    }

    @Override
    public Void visitFunctionDeclStmt(Stmt.FunctionDecl stmt) {
        JSFunction fn = new JSFunction(stmt.name.getLexeme(), stmt.params, stmt.body, environment);
        environment.define(stmt.name.getLexeme(), fn);
        explainEngine.explainFunctionDecl(stmt.name, getLine(stmt));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = JSUndefined.INSTANCE;
        if (stmt.value != null) value = evaluate(stmt.value);
        explainEngine.explainReturn(value, getLine(stmt));
        throw new ReturnSignal(value);
    }

    @Override
    public Void visitSwitchStmt(Stmt.Switch stmt) {
        Object disc = evaluate(stmt.discriminant);
        explainEngine.explainSwitch(disc, getLine(stmt));
        boolean matched = false;
        try {
            for (Stmt.Case c : stmt.cases) {
                if (!matched && c.value != null) {
                    if (strictEquals(disc, evaluate(c.value))) matched = true;
                }
                if (!matched && c.value == null) matched = true;
                if (matched) for (Stmt s : c.body) execute(s);
            }
        } catch (BreakSignal e) { /* exit switch */ }
        return null;
    }

    @Override public Void visitBreakStmt(Stmt.Break stmt) { throw new BreakSignal(); }
    @Override public Void visitContinueStmt(Stmt.Continue stmt) { throw new ContinueSignal(); }

    // ════════════════════════════════════════════════════════════════════
    //  EXPRESSION VISITORS
    // ════════════════════════════════════════════════════════════════════

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitIdentifierExpr(Expr.Identifier expr) {
        String name = expr.name.getLexeme();
        if ("this".equals(name)) {
            try {
                return environment.get("this", expr.name);
            } catch (RuntimeError e) {
                return JSUndefined.INSTANCE;
            }
        }
        return environment.get(name, expr.name);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        TokenType op = expr.operator.getType();

        Object result = switch (op) {
            case PLUS -> {
                if (left instanceof String || right instanceof String)
                    yield Stringify.toJSString(left) + Stringify.toJSString(right);
                yield toNumber(left) + toNumber(right);
            }
            case MINUS     -> toNumber(left) - toNumber(right);
            case STAR      -> toNumber(left) * toNumber(right);
            case SLASH     -> toNumber(left) / toNumber(right);
            case PERCENT   -> toNumber(left) % toNumber(right);
            case STAR_STAR -> Math.pow(toNumber(left), toNumber(right));

            case LESS          -> toNumber(left) < toNumber(right);
            case LESS_EQUAL    -> toNumber(left) <= toNumber(right);
            case GREATER       -> toNumber(left) > toNumber(right);
            case GREATER_EQUAL -> toNumber(left) >= toNumber(right);

            case EQUAL_EQUAL       -> looseEquals(left, right);
            case BANG_EQUAL        -> !looseEquals(left, right);
            case EQUAL_EQUAL_EQUAL -> strictEquals(left, right);
            case BANG_EQUAL_EQUAL  -> !strictEquals(left, right);
            case IN -> {
                String prop = Stringify.toJSString(left);
                if (right instanceof Map<?, ?> map) {
                    yield map.containsKey(prop);
                } else if (right instanceof List<?> list) {
                    try {
                        double index = toNumber(left);
                        if (index == (int) index) {
                            int idx = (int) index;
                            yield idx >= 0 && idx < list.size();
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                    yield false;
                } else if (right instanceof String s) {
                    if ("length".equals(prop)) yield true;
                    try {
                        double index = toNumber(left);
                        if (index == (int) index) {
                            int idx = (int) index;
                            yield idx >= 0 && idx < s.length();
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                    yield false;
                } else {
                    throw new RuntimeError("TypeError: Cannot use 'in' operator to search for '" + prop + "' in " + Stringify.stringify(right), expr.operator);
                }
            }
            default -> throw new RuntimeError("Unknown operator: " + expr.operator.getLexeme(), expr.operator);
        };
        explainEngine.explainBinary(expr, result, getLine(expr));
        return result;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object operand = evaluate(expr.operand);
        Object result = switch (expr.operator.getType()) {
            case MINUS -> -toNumber(operand);
            case BANG  -> !Stringify.isTruthy(operand);
            case PLUS  -> toNumber(operand);
            default -> throw new RuntimeError("Unknown unary op: " + expr.operator.getLexeme(), expr.operator);
        };
        explainEngine.explainUnary(expr, result, getLine(expr));
        return result;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.getType() == TokenType.OR_OR) {
            if (Stringify.isTruthy(left)) return left;
        } else {
            if (!Stringify.isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name.getLexeme(), value, expr.name);
        explainEngine.explainAssign(expr.name, value, getLine(expr));
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitMemberAssignExpr(Expr.MemberAssign expr) {
        Object obj = evaluate(expr.object);
        Object value = evaluate(expr.value);
        if (obj instanceof LinkedHashMap) {
            ((Map<Object, Object>) obj).put(expr.name.getLexeme(), value);
            return value;
        }
        throw new RuntimeError("Cannot set property '" + expr.name.getLexeme() + "' of " +
                Stringify.stringify(obj), expr.name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitComputedAssignExpr(Expr.ComputedAssign expr) {
        Object obj = evaluate(expr.object);
        Object index = evaluate(expr.index);
        Object value = evaluate(expr.value);

        if (obj instanceof ArrayList<?>) {
            int idx = (int) toNumber(index);
            List<Object> list = (List<Object>) obj;
            while (list.size() <= idx) list.add(JSUndefined.INSTANCE);
            list.set(idx, value);
            return value;
        }
        if (obj instanceof LinkedHashMap) {
            ((Map<Object, Object>) obj).put(getPropertyKey(index), value);
            return value;
        }
        throw new RuntimeError("Cannot set property of " + Stringify.stringify(obj),
                expr.bracket);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitCompoundAssignExpr(Expr.CompoundAssign expr) {
        Object obj = null;
        Object index = null;
        Object current = null;
        Token errorToken = expr.operator;

        if (expr.target instanceof Expr.Identifier id) {
            current = environment.get(id.name.getLexeme(), id.name);
            errorToken = id.name;
        } else if (expr.target instanceof Expr.MemberAccess mem) {
            obj = evaluate(mem.object);
            if (obj instanceof LinkedHashMap<?, ?> map) {
                current = map.get(mem.name.getLexeme());
                errorToken = mem.name;
            } else {
                throw new RuntimeError("Cannot read property of non-object", mem.name);
            }
        } else if (expr.target instanceof Expr.ComputedAccess comp) {
            obj = evaluate(comp.object);
            index = evaluate(comp.index);
            errorToken = comp.bracket;
            if (obj instanceof LinkedHashMap<?, ?> map) {
                current = map.get(getPropertyKey(index));
            } else if (obj instanceof ArrayList<?> list) {
                int idx = (int) toNumber(index);
                if (idx < 0 || idx >= list.size()) {
                    throw new RuntimeError("Index out of bounds", comp.bracket);
                }
                current = list.get(idx);
            } else {
                throw new RuntimeError("Cannot read property of non-object/array", comp.bracket);
            }
        }

        Object right = evaluate(expr.value);
        Object result = switch (expr.operator.getType()) {
            case PLUS_EQUAL -> {
                if (current instanceof String || right instanceof String)
                    yield Stringify.toJSString(current) + Stringify.toJSString(right);
                yield toNumber(current) + toNumber(right);
            }
            case MINUS_EQUAL   -> toNumber(current) - toNumber(right);
            case STAR_EQUAL    -> toNumber(current) * toNumber(right);
            case SLASH_EQUAL   -> toNumber(current) / toNumber(right);
            case PERCENT_EQUAL -> toNumber(current) % toNumber(right);
            default -> throw new RuntimeError("Unknown compound op: " + expr.operator.getLexeme(), expr.operator);
        };

        if (expr.target instanceof Expr.Identifier id) {
            environment.assign(id.name.getLexeme(), result, id.name);
        } else if (expr.target instanceof Expr.MemberAccess mem) {
            ((LinkedHashMap<Object, Object>) obj).put(mem.name.getLexeme(), result);
        } else if (expr.target instanceof Expr.ComputedAccess comp) {
            if (obj instanceof LinkedHashMap) {
                ((LinkedHashMap<Object, Object>) obj).put(getPropertyKey(index), result);
            } else {
                ((ArrayList<Object>) obj).set((int) toNumber(index), result);
            }
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitUpdateExpr(Expr.Update expr) {
        if (expr.target instanceof Expr.Identifier id) {
            Token name = id.name;
            Object current = environment.get(name.getLexeme(), name);
            double val = toNumber(current);
            double newVal = expr.operator.getType() == TokenType.PLUS_PLUS ? val + 1 : val - 1;
            environment.assign(name.getLexeme(), newVal, name);
            return expr.isPrefix ? newVal : val;
        } else if (expr.target instanceof Expr.MemberAccess mem) {
            Object obj = evaluate(mem.object);
            if (obj instanceof LinkedHashMap<?, ?> map) {
                String propName = mem.name.getLexeme();
                Object current = map.get(propName);
                double val = toNumber(current);
                double newVal = expr.operator.getType() == TokenType.PLUS_PLUS ? val + 1 : val - 1;
                ((LinkedHashMap<String, Object>) map).put(propName, newVal);
                return expr.isPrefix ? newVal : val;
            } else {
                throw new RuntimeError("Cannot update property of non-object", mem.name);
            }
        } else if (expr.target instanceof Expr.ComputedAccess comp) {
            Object obj = evaluate(comp.object);
            Object index = evaluate(comp.index);
            if (obj instanceof LinkedHashMap<?, ?> map) {
                Object propName = getPropertyKey(index);
                Object current = map.get(propName);
                double val = toNumber(current);
                double newVal = expr.operator.getType() == TokenType.PLUS_PLUS ? val + 1 : val - 1;
                ((LinkedHashMap<Object, Object>) map).put(propName, newVal);
                return expr.isPrefix ? newVal : val;
            } else if (obj instanceof ArrayList<?> list) {
                double indexNum = toNumber(index);
                int idx = (int) indexNum;
                if (idx < 0 || idx >= list.size()) {
                    throw new RuntimeError("Index out of bounds", comp.bracket);
                }
                Object current = list.get(idx);
                double val = toNumber(current);
                double newVal = expr.operator.getType() == TokenType.PLUS_PLUS ? val + 1 : val - 1;
                ((ArrayList<Object>) list).set(idx, newVal);
                return expr.isPrefix ? newVal : val;
            } else {
                throw new RuntimeError("Cannot update computed property of non-object/array", comp.bracket);
            }
        }
        throw new RuntimeError("Invalid update target", expr.operator);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitCallExpr(Expr.Call expr) {
        Object callee;
        Object receiver = null;

        if (expr.callee instanceof Expr.MemberAccess member) {
            receiver = evaluate(member.object);
            callee = evaluateMemberAccess(receiver, member.name.getLexeme(), member.name);
        } else if (expr.callee instanceof Expr.ComputedAccess computed) {
            receiver = evaluate(computed.object);
            Object index = evaluate(computed.index);
            callee = evaluateMemberAccess(receiver, getPropertyKey(index), computed.bracket);
        } else {
            callee = evaluate(expr.callee);
        }

        if (!callStack.isEmpty()) {
            StackFrame callerFrame = callStack.get(callStack.size() - 1);
            callerFrame.line = expr.paren.getLine();
            callerFrame.column = expr.paren.getColumn();
        }

        // Evaluate arguments (handle spread)
        List<Object> args = new ArrayList<>();
        for (Expr arg : expr.arguments) {
            if (arg instanceof Expr.Spread spread) {
                Object spreadVal = evaluate(spread.expression);
                args.addAll(evaluateIterable(spreadVal, expr.paren));
            } else {
                args.add(evaluate(arg));
            }
        }

        if (callee instanceof JSFunction fn) return callFunction(fn, args, receiver);
        if (callee instanceof JSCallable callable) {
            if (isConsoleLog(expr)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < args.size(); i++) {
                    sb.append(Stringify.stringify(args.get(i)));
                    if (i < args.size() - 1) sb.append(" ");
                }
                explainEngine.explainPrint(sb.toString(), expr.paren.getLine());
            } else {
                String calleeName = features.ASTPrinter.SourceCodeReconstructor.toSource(expr.callee);
                explainEngine.explainCall(calleeName, expr.paren.getLine());
            }
            return callable.call(this, args);
        }

        if (callee == JSUndefined.INSTANCE) {
            String prop = null;
            Token errorToken = expr.paren;
            Object obj = null;
            if (expr.callee instanceof Expr.MemberAccess member) {
                obj = evaluate(member.object);
                prop = member.name.getLexeme();
                errorToken = member.name;
            } else if (expr.callee instanceof Expr.ComputedAccess computed) {
                obj = evaluate(computed.object);
                Object index = evaluate(computed.index);
                prop = Stringify.toJSString(index);
                errorToken = computed.bracket;
            }
            if (obj != null && prop != null) {
                java.util.List<String> candidates = null;
                if (obj == globals.get("Math")) {
                    candidates = java.util.List.of("PI", "E", "floor", "ceil", "round", "abs", "sqrt", "pow", "random", "max", "min");
                } else if (obj == globals.get("Object")) {
                    candidates = java.util.List.of("keys", "values", "entries");
                } else if (obj == globals.get("console")) {
                    candidates = java.util.List.of("log");
                } else if (obj instanceof thunderjs.runtime.DateConstructor) {
                    candidates = java.util.List.of("now", "parse", "UTC");
                } else if (obj instanceof thunderjs.runtime.DateObject) {
                    candidates = java.util.List.of("getFullYear", "getMonth", "getDate", "getDay", "getHours", "getMinutes", "getSeconds", "getMilliseconds", "getTime", "getTimezoneOffset", "setFullYear", "setMonth", "setDate", "setHours", "setMinutes", "setSeconds", "setMilliseconds", "setTime", "toString", "toISOString", "toUTCString", "toDateString", "toTimeString", "toJSON", "valueOf");
                } else if (obj instanceof String) {
                    candidates = java.util.List.of("toUpperCase", "toLowerCase", "trim", "trimStart", "trimEnd", "split", "replace", "replaceAll", "substring", "slice", "indexOf", "lastIndexOf", "includes", "startsWith", "endsWith", "charAt", "charCodeAt", "repeat", "padStart", "padEnd", "concat");
                } else if (obj instanceof ArrayList<?>) {
                    candidates = java.util.List.of("length", "push", "pop", "shift", "unshift", "reverse", "join", "concat", "slice", "indexOf", "lastIndexOf", "includes", "forEach", "map", "filter", "reduce", "find", "findIndex", "some", "every", "flat", "fill");
                }
                if (candidates != null) {
                    String closest = SuggestionEngine.suggestProperty(prop, candidates);
                    if (closest != null) {
                        throw new RuntimeError("TypeError: " + prop + " is not a function", errorToken, closest);
                    }
                }
            }
        }

        throw new RuntimeError("TypeError: " + Stringify.stringify(callee) + " is not a function",
                expr.paren);
    }

    @Override
    public Object visitNewExpr(Expr.New expr) {
        Object constructor = evaluate(expr.constructor);

        if (!callStack.isEmpty()) {
            StackFrame callerFrame = callStack.get(callStack.size() - 1);
            callerFrame.line = expr.keyword.getLine();
            callerFrame.column = expr.keyword.getColumn();
        }

        List<Object> args = new ArrayList<>();
        if (expr.arguments != null) {
            for (Expr arg : expr.arguments) {
                if (arg instanceof Expr.Spread spread) {
                    Object spreadVal = evaluate(spread.expression);
                    args.addAll(evaluateIterable(spreadVal, expr.keyword));
                } else {
                    args.add(evaluate(arg));
                }
            }
        }

        if (constructor instanceof DateConstructor) {
            JSDate date;
            if (args.isEmpty()) {
                date = new JSDate();
            } else if (args.size() == 1) {
                Object arg = args.get(0);
                if (arg instanceof Double d) {
                    date = new JSDate(d);
                } else if (arg instanceof String s) {
                    date = new JSDate(s);
                } else if (arg instanceof DateObject otherDate) {
                    date = new JSDate(otherDate.getJSDate().getTime());
                } else {
                    double d = Stringify.toJSNumber(arg);
                    if (!Double.isNaN(d)) {
                        date = new JSDate(d);
                    } else {
                        date = new JSDate(Stringify.toJSString(arg));
                    }
                }
            } else {
                double y = args.size() > 0 ? Stringify.toJSNumber(args.get(0)) : Double.NaN;
                double m = args.size() > 1 ? Stringify.toJSNumber(args.get(1)) : 0;
                double d = args.size() > 2 ? Stringify.toJSNumber(args.get(2)) : 1;
                double hh = args.size() > 3 ? Stringify.toJSNumber(args.get(3)) : 0;
                double mm = args.size() > 4 ? Stringify.toJSNumber(args.get(4)) : 0;
                double ss = args.size() > 5 ? Stringify.toJSNumber(args.get(5)) : 0;
                double ms = args.size() > 6 ? Stringify.toJSNumber(args.get(6)) : 0;
                date = new JSDate(y, m, d, hh, mm, ss, ms);
            }
            return new DateObject(date);
        }
        if (constructor instanceof SetConstructor) {
            return ((SetConstructor) constructor).call(this, args);
        }
        if (constructor instanceof MapConstructor) {
            return ((MapConstructor) constructor).call(this, args);
        }

        throw new RuntimeError("TypeError: " + Stringify.stringify(constructor) + " is not a constructor",
                expr.keyword);
    }

    private boolean isConsoleLog(Expr.Call call) {
        if (call.callee instanceof Expr.MemberAccess member) {
            if (member.name.getLexeme().equals("log")) {
                if (member.object instanceof Expr.Identifier id) {
                    return id.name.getLexeme().equals("console");
                }
            }
        }
        return false;
    }

    /**
     * Call a user-defined function.
     */
    public Object callFunction(JSFunction fn, List<Object> args) {
        return callFunction(fn, args, null);
    }

    public Object callFunction(JSFunction fn, List<Object> args, Object thisVal) {
        int line = fn.getParams().isEmpty() ? 0 : fn.getParams().get(0).name.getLine();
        explainEngine.explainCall(fn.name(), line);
        traceEngine.push(fn.name(), line);

        List<String> paramNames = new ArrayList<>();
        for (Expr.Parameter p : fn.getParams()) {
            paramNames.add(p.name.getLexeme());
        }
        traceEngine.beforeCall(fn.name(), args, paramNames);

        StackFrame frame = new StackFrame(fn.name(), -1, -1);
        callStack.add(frame);

        Object result = null;
        boolean success = false;
        try {
            Environment funcEnv = new Environment(fn.getClosure());
            if (thisVal != null) {
                funcEnv.define("this", thisVal);
            }

            List<Expr.Parameter> params = fn.getParams();
            for (int i = 0; i < params.size(); i++) {
                Expr.Parameter param = params.get(i);
                if (param.isRest) {
                    List<Object> rest = new ArrayList<>();
                    for (int j = i; j < args.size(); j++) rest.add(args.get(j));
                    funcEnv.define(param.name.getLexeme(), rest);
                    break;
                }
                Object value = i < args.size() ? args.get(i) : JSUndefined.INSTANCE;
                if ((value instanceof JSUndefined) && param.defaultValue != null)
                    value = evaluate(param.defaultValue);
                funcEnv.define(param.name.getLexeme(), value);
            }

            if (fn.hasConciseBody()) {
                Environment previous = this.environment;
                try {
                    this.environment = funcEnv;
                    result = evaluate(fn.getConciseBody());
                    success = true;
                    return result;
                } finally { this.environment = previous; }
            }

            try {
                executeBlock(fn.getBody(), funcEnv);
            } catch (ReturnSignal ret) {
                result = ret.value;
                success = true;
                return result;
            }
            result = JSUndefined.INSTANCE;
            success = true;
            return result;
        } catch (RuntimeError e) {
            if (e.getCallStack() == null) {
                frame.line = e.getLine();
                frame.column = e.getColumn();
                e.setCallStack(new ArrayList<>(callStack));
            }
            throw e;
        } finally {
            if (success) {
                traceEngine.afterCall(fn.name(), result);
            }
            traceEngine.pop();
            if (!callStack.isEmpty()) {
                callStack.remove(callStack.size() - 1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object evaluateMemberAccess(Object object, Object prop, Token token) {
        String propStr = Stringify.toJSString(prop);
        if (object instanceof DateObject dateObj) {
            return dateObj.getProperty(propStr, token != null ? token.getLine() : 0);
        }
        if (object instanceof DateConstructor dateConst) {
            return dateConst.getProperty(propStr);
        }
        if (object instanceof SetObject setObj) {
            return setObj.getProperty(propStr, token != null ? token.getLine() : 0);
        }
        if (object instanceof MapObject mapObj) {
            return mapObj.getProperty(propStr, token != null ? token.getLine() : 0);
        }

        // ── String properties/methods ───────────────────────────────
        if (object instanceof String s) return getStringProperty(s, propStr, token);

        // ── Array properties/methods ────────────────────────────────
        if (object instanceof ArrayList<?> arr) return getArrayProperty((List<Object>) arr, propStr, token);

        // ── Object properties ───────────────────────────────────────
        if (object instanceof LinkedHashMap<?, ?> map) {
            Object val = ((Map<Object, Object>) map).get(prop);
            if (val != null) return val;

            // Prototype lookup
            if (prop instanceof String) {
                if ("hasOwnProperty".equals(propStr)) {
                    return (JSCallable) (interpreter, args) -> {
                        if (args.isEmpty()) return false;
                        Object checkProp = getPropertyKey(args.get(0));
                        return map.containsKey(checkProp);
                    };
                }
                if ("toString".equals(propStr)) {
                    return (JSCallable) (interpreter, args) -> "[object Object]";
                }
            }

            java.util.List<String> keys = new java.util.ArrayList<>();
            for (Object k : map.keySet()) {
                if (k instanceof String) keys.add((String) k);
            }
            String closest = SuggestionEngine.suggestProperty(propStr, keys);
            if (closest != null && token != null) {
                throw new RuntimeError("TypeError: Cannot read property '" + propStr + "'", token, closest);
            }
            return JSUndefined.INSTANCE;
        }

        if (token != null) {
            throw new RuntimeError("TypeError: Cannot read property '" + propStr + "' of " +
                    Stringify.stringify(object), token);
        } else {
            throw new RuntimeError("TypeError: Cannot read property '" + propStr + "' of " +
                    Stringify.stringify(object), 0);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitMemberAccessExpr(Expr.MemberAccess expr) {
        Object object = evaluate(expr.object);
        String prop = expr.name.getLexeme();
        return evaluateMemberAccess(object, prop, expr.name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitComputedAccessExpr(Expr.ComputedAccess expr) {
        Object object = evaluate(expr.object);
        Object index = evaluate(expr.index);
        Object prop = getPropertyKey(index);

        if (object instanceof ArrayList<?> arr) {
            double idxD = toNumber(index);
            int idx = (int) idxD;
            if (idx >= 0 && idx < arr.size()) return arr.get(idx);
            if (idxD == (int) idxD) return JSUndefined.INSTANCE; // Out of bounds integer index
        }
        if (object instanceof String s) {
            double idxD = toNumber(index);
            int idx = (int) idxD;
            if (idx >= 0 && idx < s.length()) return String.valueOf(s.charAt(idx));
            if (idxD == (int) idxD) return JSUndefined.INSTANCE; // Out of bounds integer index
        }

        return evaluateMemberAccess(object, prop, expr.bracket);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitArrayLiteralExpr(Expr.ArrayLiteral expr) {
        List<Object> elements = new ArrayList<>();
        for (Expr el : expr.elements) {
            if (el instanceof Expr.Spread spread) {
                Object sv = evaluate(spread.expression);
                elements.addAll(evaluateIterable(sv, expr.bracket));
            } else {
                elements.add(evaluate(el));
            }
        }
        return elements;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitObjectLiteralExpr(Expr.ObjectLiteral expr) {
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < expr.keys.size(); i++) {
            Object keyObj = expr.keys.get(i);
            Object value = evaluate(expr.values.get(i));
            if (keyObj == null) {
                if (value instanceof LinkedHashMap<?, ?> spreadMap) {
                    for (Map.Entry<?, ?> entry : spreadMap.entrySet()) {
                        map.put(entry.getKey(), entry.getValue());
                    }
                } else if (value instanceof ArrayList<?> list) {
                    for (int j = 0; j < list.size(); j++) {
                        map.put(String.valueOf(j), list.get(j));
                    }
                } else if (value instanceof String str) {
                    for (int j = 0; j < str.length(); j++) {
                        map.put(String.valueOf(j), String.valueOf(str.charAt(j)));
                    }
                }
            } else {
                Object key;
                if (keyObj instanceof Expr computedKeyExpr) {
                    key = getPropertyKey(evaluate(computedKeyExpr));
                } else {
                    key = (String) keyObj;
                }
                map.put(key, value);
            }
        }
        return map;
    }

    @Override
    public Object visitFunctionExpr(Expr.FunctionExpr expr) {
        String name = expr.name != null ? expr.name.getLexeme() : null;
        return new JSFunction(name, expr.params, expr.body, environment);
    }

    @Override
    public Object visitArrowFunctionExpr(Expr.ArrowFunction expr) {
        if (expr.expression != null)
            return new JSFunction(null, expr.params, expr.expression, environment);
        return new JSFunction(null, expr.params, expr.body, environment);
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        return Stringify.isTruthy(evaluate(expr.condition))
                ? evaluate(expr.thenBranch) : evaluate(expr.elseBranch);
    }

    @Override
    public Object visitSpreadExpr(Expr.Spread expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitTemplateLiteralExpr(Expr.TemplateLiteral expr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expr.parts.size(); i++) {
            sb.append(expr.parts.get(i));
            if (i < expr.expressions.size())
                sb.append(Stringify.toJSString(evaluate(expr.expressions.get(i))));
        }
        return sb.toString();
    }

    @Override
    public Object visitTypeofExpr(Expr.TypeofExpr expr) {
        Object val;
        try { val = evaluate(expr.operand); }
        catch (RuntimeError e) { return "undefined"; }

        if (val == null || val instanceof JSUndefined) return "undefined";
        if (val instanceof JSNull) return "object";
        if (val instanceof Double) return "number";
        if (val instanceof String) return "string";
        if (val instanceof Boolean) return "boolean";
        if (val instanceof JSSymbol) return "symbol";
        if (val instanceof JSCallable || val instanceof JSFunction) return "function";
        return "object";
    }

    // ════════════════════════════════════════════════════════════════════
    //  BUILT-IN PROPERTY/METHOD DISPATCH
    // ════════════════════════════════════════════════════════════════════

    private Object getMathProperty(String name) {
        return switch (name) {
            case "PI" -> MathObject.PI;
            case "E"  -> MathObject.E;
            case "floor"  -> (JSCallable) (i, a) -> MathObject.floor(toNumber(a.get(0)));
            case "ceil"   -> (JSCallable) (i, a) -> MathObject.ceil(toNumber(a.get(0)));
            case "round"  -> (JSCallable) (i, a) -> MathObject.round(toNumber(a.get(0)));
            case "abs"    -> (JSCallable) (i, a) -> MathObject.abs(toNumber(a.get(0)));
            case "sqrt"   -> (JSCallable) (i, a) -> MathObject.sqrt(toNumber(a.get(0)));
            case "pow"    -> (JSCallable) (i, a) -> MathObject.pow(toNumber(a.get(0)), toNumber(a.get(1)));
            case "random" -> (JSCallable) (i, a) -> MathObject.random();
            case "max"    -> (JSCallable) (i, a) -> {
                double[] nums = a.stream().mapToDouble(this::toNumber).toArray();
                return MathObject.max(nums);
            };
            case "min"    -> (JSCallable) (i, a) -> {
                double[] nums = a.stream().mapToDouble(this::toNumber).toArray();
                return MathObject.min(nums);
            };
            default -> throw new RuntimeError("TypeError: Math." + name + " is not a function");
        };
    }

    @SuppressWarnings("unchecked")
    private Object getObjectStaticMethod(String name) {
        return switch (name) {
            case "keys" -> (JSCallable) (i, a) -> {
                if (a.isEmpty()) throw new RuntimeError("TypeError: Cannot convert undefined or null to object");
                Object obj = a.get(0);
                if (obj == null || obj instanceof JSUndefined || obj instanceof JSNull) {
                    throw new RuntimeError("TypeError: Cannot convert undefined or null to object");
                }
                if (obj instanceof LinkedHashMap<?, ?> map) {
                    List<Object> keys = new ArrayList<>();
                    for (Object k : map.keySet()) {
                        if (k instanceof String) {
                            keys.add(k);
                        }
                    }
                    return keys;
                }
                if (obj instanceof String s) {
                    List<Object> keys = new ArrayList<>();
                    for (int idx = 0; idx < s.length(); idx++) {
                        keys.add(String.valueOf(idx));
                    }
                    return keys;
                }
                if (obj instanceof ArrayList<?> list) {
                    List<Object> keys = new ArrayList<>();
                    for (int idx = 0; idx < list.size(); idx++) {
                        keys.add(String.valueOf(idx));
                    }
                    return keys;
                }
                return new ArrayList<>();
            };
            case "values" -> (JSCallable) (i, a) -> {
                if (a.isEmpty()) throw new RuntimeError("TypeError: Cannot convert undefined or null to object");
                Object obj = a.get(0);
                if (obj == null || obj instanceof JSUndefined || obj instanceof JSNull) {
                    throw new RuntimeError("TypeError: Cannot convert undefined or null to object");
                }
                if (obj instanceof LinkedHashMap<?, ?> map) {
                    List<Object> values = new ArrayList<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getKey() instanceof String) {
                            values.add(entry.getValue());
                        }
                    }
                    return values;
                }
                if (obj instanceof String s) {
                    List<Object> values = new ArrayList<>();
                    for (int idx = 0; idx < s.length(); idx++) {
                        values.add(String.valueOf(s.charAt(idx)));
                    }
                    return values;
                }
                if (obj instanceof ArrayList<?> list) {
                    return new ArrayList<>(list);
                }
                return new ArrayList<>();
            };
            case "entries" -> (JSCallable) (i, a) -> {
                if (a.isEmpty()) throw new RuntimeError("TypeError: Cannot convert undefined or null to object");
                Object obj = a.get(0);
                if (obj == null || obj instanceof JSUndefined || obj instanceof JSNull) {
                    throw new RuntimeError("TypeError: Cannot convert undefined or null to object");
                }
                if (obj instanceof LinkedHashMap<?, ?> map) {
                    List<Object> entries = new ArrayList<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getKey() instanceof String) {
                            List<Object> pair = new ArrayList<>();
                            pair.add(entry.getKey()); pair.add(entry.getValue());
                            entries.add(pair);
                        }
                    }
                    return entries;
                }
                if (obj instanceof String s) {
                    List<Object> entries = new ArrayList<>();
                    for (int idx = 0; idx < s.length(); idx++) {
                        List<Object> pair = new ArrayList<>();
                        pair.add(String.valueOf(idx)); pair.add(String.valueOf(s.charAt(idx)));
                        entries.add(pair);
                    }
                    return entries;
                }
                if (obj instanceof ArrayList<?> list) {
                    List<Object> entries = new ArrayList<>();
                    for (int idx = 0; idx < list.size(); idx++) {
                        List<Object> pair = new ArrayList<>();
                        pair.add(String.valueOf(idx)); pair.add(list.get(idx));
                        entries.add(pair);
                    }
                    return entries;
                }
                return new ArrayList<>();
            };
            case "getOwnPropertySymbols" -> (JSCallable) (i, a) -> {
                if (a.isEmpty()) throw new RuntimeError("TypeError: Cannot convert undefined or null to object");
                Object obj = a.get(0);
                if (obj == null || obj instanceof JSUndefined || obj instanceof JSNull) {
                    throw new RuntimeError("TypeError: Cannot convert undefined or null to object");
                }
                List<Object> symbols = new ArrayList<>();
                if (obj instanceof LinkedHashMap<?, ?> map) {
                    for (Object k : map.keySet()) {
                        if (k instanceof JSSymbol) {
                            symbols.add(k);
                        }
                    }
                }
                return symbols;
            };
            default -> throw new RuntimeError("TypeError: Object." + name + " is not a function");
        };
    }

    // ── String methods ──────────────────────────────────────────────────

    private Object getStringProperty(String s, String prop, Token token) {
        return switch (prop) {
            case "length"      -> (double) s.length();
            case "toUpperCase" -> (JSCallable) (i, a) -> s.toUpperCase();
            case "toLowerCase" -> (JSCallable) (i, a) -> s.toLowerCase();
            case "trim"        -> (JSCallable) (i, a) -> s.trim();
            case "trimStart"   -> (JSCallable) (i, a) -> s.stripLeading();
            case "trimEnd"     -> (JSCallable) (i, a) -> s.stripTrailing();
            case "split" -> (JSCallable) (i, a) -> {
                String sep = a.isEmpty() ? null : Stringify.toJSString(a.get(0));
                if (sep == null) { List<Object> r = new ArrayList<>(); r.add(s); return r; }
                String[] parts;
                if (sep.isEmpty()) {
                    parts = new String[s.length()];
                    for (int j = 0; j < s.length(); j++) parts[j] = String.valueOf(s.charAt(j));
                } else {
                    parts = s.split(java.util.regex.Pattern.quote(sep), -1);
                }
                List<Object> r = new ArrayList<>();
                for (String p : parts) r.add(p);
                return r;
            };
            case "replace" -> (JSCallable) (i, a) -> {
                String search = Stringify.toJSString(a.get(0));
                String repl = Stringify.toJSString(a.get(1));
                int idx = s.indexOf(search);
                if (idx < 0) return s;
                return s.substring(0, idx) + repl + s.substring(idx + search.length());
            };
            case "replaceAll" -> (JSCallable) (i, a) ->
                    s.replace(Stringify.toJSString(a.get(0)), Stringify.toJSString(a.get(1)));
            case "substring" -> (JSCallable) (i, a) -> {
                int start = (int) toNumber(a.get(0));
                if (a.size() > 1) {
                    int end = (int) toNumber(a.get(1));
                    return s.substring(Math.max(0, start), Math.min(s.length(), end));
                }
                return s.substring(Math.max(0, start));
            };
            case "slice" -> (JSCallable) (i, a) -> {
                int start = (int) toNumber(a.get(0));
                if (start < 0) start = s.length() + start;
                if (a.size() > 1) {
                    int end = (int) toNumber(a.get(1));
                    if (end < 0) end = s.length() + end;
                    return s.substring(Math.max(0, start), Math.min(s.length(), end));
                }
                return s.substring(Math.max(0, start));
            };
            case "indexOf"    -> (JSCallable) (i, a) -> (double) s.indexOf(Stringify.toJSString(a.get(0)));
            case "lastIndexOf"-> (JSCallable) (i, a) -> (double) s.lastIndexOf(Stringify.toJSString(a.get(0)));
            case "includes"   -> (JSCallable) (i, a) -> s.contains(Stringify.toJSString(a.get(0)));
            case "startsWith" -> (JSCallable) (i, a) -> s.startsWith(Stringify.toJSString(a.get(0)));
            case "endsWith"   -> (JSCallable) (i, a) -> s.endsWith(Stringify.toJSString(a.get(0)));
            case "charAt" -> (JSCallable) (i, a) -> {
                int idx = (int) toNumber(a.get(0));
                if (idx < 0 || idx >= s.length()) return "";
                return String.valueOf(s.charAt(idx));
            };
            case "charCodeAt" -> (JSCallable) (i, a) -> {
                int idx = (int) toNumber(a.get(0));
                if (idx < 0 || idx >= s.length()) return Double.NaN;
                return (double) s.charAt(idx);
            };
            case "repeat" -> (JSCallable) (i, a) -> s.repeat(Math.max(0, (int) toNumber(a.get(0))));
            case "padStart" -> (JSCallable) (i, a) -> {
                int tgt = (int) toNumber(a.get(0));
                String pad = a.size() > 1 ? Stringify.toJSString(a.get(1)) : " ";
                StringBuilder sb = new StringBuilder(s);
                while (sb.length() < tgt) sb.insert(0, pad);
                return sb.substring(sb.length() - tgt);
            };
            case "padEnd" -> (JSCallable) (i, a) -> {
                int tgt = (int) toNumber(a.get(0));
                String pad = a.size() > 1 ? Stringify.toJSString(a.get(1)) : " ";
                StringBuilder sb = new StringBuilder(s);
                while (sb.length() < tgt) sb.append(pad);
                return sb.substring(0, tgt);
            };
            case "concat" -> (JSCallable) (i, a) -> {
                StringBuilder sb = new StringBuilder(s);
                for (Object arg : a) sb.append(Stringify.toJSString(arg));
                return sb.toString();
            };
            case "toString" -> (JSCallable) (i, a) -> s;
            default -> throw new RuntimeError(
                    "TypeError: \"" + s + "\"." + prop + " is not a function", token);
        };
    }

    // ── Array methods ───────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Object getArrayProperty(List<Object> arr, String prop, Token token) {
        return switch (prop) {
            case "length" -> (double) arr.size();
            case "push"    -> (JSCallable) (i, a) -> { arr.addAll(a); return (double) arr.size(); };
            case "pop"     -> (JSCallable) (i, a) -> arr.isEmpty() ? JSUndefined.INSTANCE : arr.remove(arr.size()-1);
            case "shift"   -> (JSCallable) (i, a) -> arr.isEmpty() ? JSUndefined.INSTANCE : arr.remove(0);
            case "unshift" -> (JSCallable) (i, a) -> {
                for (int j = a.size()-1; j >= 0; j--) arr.add(0, a.get(j));
                return (double) arr.size();
            };
            case "reverse" -> (JSCallable) (i, a) -> { Collections.reverse(arr); return arr; };
            case "join" -> (JSCallable) (i, a) -> {
                String sep = a.isEmpty() ? "," : Stringify.toJSString(a.get(0));
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < arr.size(); j++) {
                    if (j > 0) sb.append(sep);
                    Object el = arr.get(j);
                    if (el != null && !(el instanceof JSNull) && !(el instanceof JSUndefined))
                        sb.append(Stringify.toJSString(el));
                }
                return sb.toString();
            };
            case "includes" -> (JSCallable) (i, a) -> {
                for (Object el : arr) if (strictEquals(el, a.get(0))) return true;
                return false;
            };
            case "indexOf" -> (JSCallable) (i, a) -> {
                for (int j = 0; j < arr.size(); j++) if (strictEquals(arr.get(j), a.get(0))) return (double) j;
                return -1.0;
            };
            case "lastIndexOf" -> (JSCallable) (i, a) -> {
                for (int j = arr.size()-1; j >= 0; j--) if (strictEquals(arr.get(j), a.get(0))) return (double) j;
                return -1.0;
            };
            case "slice" -> (JSCallable) (i, a) -> {
                int start = a.isEmpty() ? 0 : (int) toNumber(a.get(0));
                int end = a.size() > 1 ? (int) toNumber(a.get(1)) : arr.size();
                if (start < 0) start = arr.size() + start;
                if (end < 0) end = arr.size() + end;
                start = Math.max(0, Math.min(start, arr.size()));
                end = Math.max(0, Math.min(end, arr.size()));
                return new ArrayList<>(arr.subList(start, end));
            };
            case "splice" -> (JSCallable) (i, a) -> {
                int start = a.isEmpty() ? 0 : (int) toNumber(a.get(0));
                if (start < 0) start = arr.size() + start;
                start = Math.max(0, Math.min(start, arr.size()));
                int del = a.size() > 1 ? (int) toNumber(a.get(1)) : arr.size() - start;
                del = Math.max(0, Math.min(del, arr.size() - start));
                List<Object> removed = new ArrayList<>();
                for (int j = 0; j < del; j++) removed.add(arr.remove(start));
                for (int j = 2; j < a.size(); j++) arr.add(start + (j-2), a.get(j));
                return removed;
            };
            case "concat" -> (JSCallable) (i, a) -> {
                List<Object> result = new ArrayList<>(arr);
                for (Object arg : a) {
                    if (arg instanceof ArrayList<?>) result.addAll((List<Object>) arg);
                    else result.add(arg);
                }
                return result;
            };
            case "sort" -> (JSCallable) (i, a) -> {
                if (a.isEmpty()) {
                    arr.sort((x, y) -> Stringify.toJSString(x).compareTo(Stringify.toJSString(y)));
                } else if (a.get(0) instanceof JSCallable cb) {
                    arr.sort((x, y) -> {
                        List<Object> cmpArgs = new ArrayList<>(); cmpArgs.add(x); cmpArgs.add(y);
                        return (int) toNumber(invokeCallback(cb, cmpArgs, token));
                    });
                }
                return arr;
            };
            case "toString" -> (JSCallable) (i, a) -> Stringify.stringify(arr);

            // ── Higher-order methods ────────────────────────────────
            case "map" -> (JSCallable) (i, a) -> {
                Object cbVal = a.isEmpty() ? null : a.get(0);
                List<Object> result = new ArrayList<>();
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cbArgs = new ArrayList<>(); cbArgs.add(arr.get(j)); cbArgs.add((double)j); cbArgs.add(arr);
                    result.add(invokeCallback(cbVal, cbArgs, token));
                }
                return result;
            };
            case "filter" -> (JSCallable) (i, a) -> {
                Object cbVal = a.isEmpty() ? null : a.get(0);
                List<Object> result = new ArrayList<>();
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cbArgs = new ArrayList<>(); cbArgs.add(arr.get(j)); cbArgs.add((double)j); cbArgs.add(arr);
                    if (Stringify.isTruthy(invokeCallback(cbVal, cbArgs, token))) result.add(arr.get(j));
                }
                return result;
            };
            case "reduce" -> (JSCallable) (i, a) -> {
                Object cbVal = a.isEmpty() ? null : a.get(0);
                Object acc; int startIdx;
                if (a.size() > 1) { acc = a.get(1); startIdx = 0; }
                else { if (arr.isEmpty()) throw new RuntimeError("Reduce of empty array with no initial value", token); acc = arr.get(0); startIdx = 1; }
                for (int j = startIdx; j < arr.size(); j++) {
                    List<Object> cbArgs = new ArrayList<>(); cbArgs.add(acc); cbArgs.add(arr.get(j)); cbArgs.add((double)j); cbArgs.add(arr);
                    acc = invokeCallback(cbVal, cbArgs, token);
                }
                return acc;
            };
            case "find" -> (JSCallable) (i, a) -> {
                Object cbVal = a.isEmpty() ? null : a.get(0);
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cbArgs = new ArrayList<>(); cbArgs.add(arr.get(j)); cbArgs.add((double)j);
                    if (Stringify.isTruthy(invokeCallback(cbVal, cbArgs, token))) return arr.get(j);
                }
                return JSUndefined.INSTANCE;
            };
            case "findIndex" -> (JSCallable) (i, a) -> {
                Object cbVal = a.isEmpty() ? null : a.get(0);
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cbArgs = new ArrayList<>(); cbArgs.add(arr.get(j)); cbArgs.add((double)j);
                    if (Stringify.isTruthy(invokeCallback(cbVal, cbArgs, token))) return (double) j;
                }
                return -1.0;
            };
            case "some" -> (JSCallable) (i, a) -> {
                Object cbVal = a.isEmpty() ? null : a.get(0);
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cbArgs = new ArrayList<>(); cbArgs.add(arr.get(j)); cbArgs.add((double)j);
                    if (Stringify.isTruthy(invokeCallback(cbVal, cbArgs, token))) return true;
                }
                return false;
            };
            case "every" -> (JSCallable) (i, a) -> {
                Object cbVal = a.isEmpty() ? null : a.get(0);
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cbArgs = new ArrayList<>(); cbArgs.add(arr.get(j)); cbArgs.add((double)j);
                    if (!Stringify.isTruthy(invokeCallback(cbVal, cbArgs, token))) return false;
                }
                return true;
            };
            case "forEach" -> (JSCallable) (i, a) -> {
                Object cbVal = a.isEmpty() ? null : a.get(0);
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cbArgs = new ArrayList<>(); cbArgs.add(arr.get(j)); cbArgs.add((double)j); cbArgs.add(arr);
                    invokeCallback(cbVal, cbArgs, token);
                }
                return JSUndefined.INSTANCE;
            };
            case "flat" -> (JSCallable) (i, a) -> {
                int depth = a.isEmpty() ? 1 : (int) toNumber(a.get(0));
                return flattenArray(arr, depth);
            };
            case "fill" -> (JSCallable) (i, a) -> {
                Object fv = a.isEmpty() ? JSUndefined.INSTANCE : a.get(0);
                int start = a.size() > 1 ? (int) toNumber(a.get(1)) : 0;
                int end = a.size() > 2 ? (int) toNumber(a.get(2)) : arr.size();
                if (start < 0) start = arr.size() + start;
                if (end < 0) end = arr.size() + end;
                for (int j = Math.max(0,start); j < Math.min(arr.size(),end); j++) arr.set(j, fv);
                return arr;
            };
            case "entries" -> (JSCallable) (i, a) -> {
                List<List<Object>> entries = new ArrayList<>();
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> entry = new ArrayList<>();
                    entry.add((double) j);
                    entry.add(arr.get(j));
                    entries.add(entry);
                }
                return entries;
            };
            default -> throw new RuntimeError("TypeError: arr." + prop + " is not a function", token);
        };
    }

    @SuppressWarnings("unchecked")
    private List<Object> flattenArray(List<Object> arr, int depth) {
        List<Object> result = new ArrayList<>();
        for (Object el : arr) {
            if (depth > 0 && el instanceof ArrayList<?>)
                result.addAll(flattenArray((List<Object>) el, depth - 1));
            else result.add(el);
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════
    //  EQUALITY HELPERS
    // ════════════════════════════════════════════════════════════════════

    public boolean strictEquals(Object a, Object b) {
        if (a instanceof JSNull && b instanceof JSNull) return true;
        if (a instanceof JSUndefined && b instanceof JSUndefined) return true;
        if (a instanceof JSNull || b instanceof JSNull) return false;
        if (a instanceof JSUndefined || b instanceof JSUndefined) return false;
        if (a instanceof Double da && b instanceof Double db) {
            if (Double.isNaN(da) || Double.isNaN(db)) return false;
            return da.equals(db);
        }
        if (a instanceof String && b instanceof String) return a.equals(b);
        if (a instanceof Boolean && b instanceof Boolean) return a.equals(b);
        return a == b;
    }

    private boolean looseEquals(Object a, Object b) {
        if (sameType(a, b)) return strictEquals(a, b);
        if ((a instanceof JSNull && b instanceof JSUndefined) ||
            (a instanceof JSUndefined && b instanceof JSNull)) return true;
        if (a instanceof Double && b instanceof String) return looseEquals(a, Stringify.toJSNumber(b));
        if (a instanceof String && b instanceof Double) return looseEquals(Stringify.toJSNumber(a), b);
        if (a instanceof Boolean) return looseEquals(Stringify.toJSNumber(a), b);
        if (b instanceof Boolean) return looseEquals(a, Stringify.toJSNumber(b));
        return false;
    }

    private boolean sameType(Object a, Object b) {
        if (a instanceof Double && b instanceof Double) return true;
        if (a instanceof String && b instanceof String) return true;
        if (a instanceof Boolean && b instanceof Boolean) return true;
        if (a instanceof JSNull && b instanceof JSNull) return true;
        if (a instanceof JSUndefined && b instanceof JSUndefined) return true;
        return false;
    }

    private double toNumber(Object value) { return Stringify.toJSNumber(value); }

    public Object invokeCallback(Object callback, List<Object> arguments, Token token) {
        if (callback instanceof JSFunction fn) {
            return callFunction(fn, arguments);
        } else if (callback instanceof JSCallable callable) {
            return callable.call(this, arguments);
        } else {
            throw new RuntimeError("TypeError: " + Stringify.stringify(callback) + " is not a function", token);
        }
    }

    public int getLine(Expr expr) {
        if (expr == null) return 0;
        if (expr instanceof Expr.Literal e) return e.token != null ? e.token.getLine() : 0;
        if (expr instanceof Expr.Identifier e) return e.name != null ? e.name.getLine() : 0;
        if (expr instanceof Expr.Binary e) return e.operator != null ? e.operator.getLine() : 0;
        if (expr instanceof Expr.Unary e) return e.operator != null ? e.operator.getLine() : 0;
        if (expr instanceof Expr.Logical e) return e.operator != null ? e.operator.getLine() : 0;
        if (expr instanceof Expr.Assign e) return e.name != null ? e.name.getLine() : 0;
        if (expr instanceof Expr.MemberAssign e) return e.name != null ? e.name.getLine() : 0;
        if (expr instanceof Expr.ComputedAssign e) return e.bracket != null ? e.bracket.getLine() : 0;
        if (expr instanceof Expr.CompoundAssign e) return e.operator != null ? e.operator.getLine() : 0;
        if (expr instanceof Expr.Update e) return e.operator != null ? e.operator.getLine() : 0;
        if (expr instanceof Expr.Call e) return e.paren != null ? e.paren.getLine() : 0;
        if (expr instanceof Expr.MemberAccess e) return e.name != null ? e.name.getLine() : 0;
        if (expr instanceof Expr.ComputedAccess e) return e.bracket != null ? e.bracket.getLine() : 0;
        if (expr instanceof Expr.Ternary e) return getLine(e.condition);
        if (expr instanceof Expr.TemplateLiteral e) return e.token != null ? e.token.getLine() : 0;
        if (expr instanceof Expr.Grouping e) return getLine(e.expression);
        if (expr instanceof Expr.TypeofExpr e) return e.keyword != null ? e.keyword.getLine() : 0;
        if (expr instanceof Expr.DestructuredAssign e) return getLine(e.pattern);
        return 0;
    }

    public int getLine(Stmt stmt) {
        if (stmt == null) return 0;
        if (stmt instanceof Stmt.ExpressionStmt e) return getLine(e.expression);
        if (stmt instanceof Stmt.VarDeclaration e) return e.name != null ? e.name.getLine() : 0;
        if (stmt instanceof Stmt.Block e) return e.statements.isEmpty() ? 0 : getLine(e.statements.get(0));
        if (stmt instanceof Stmt.If e) return getLine(e.condition);
        if (stmt instanceof Stmt.For e) {
            if (e.initializer != null) return getLine(e.initializer);
            if (e.condition != null) return getLine(e.condition);
            return 0;
        }
        if (stmt instanceof Stmt.While e) return getLine(e.condition);
        if (stmt instanceof Stmt.DoWhile e) return getLine(e.condition);
        if (stmt instanceof Stmt.FunctionDecl e) return e.name != null ? e.name.getLine() : 0;
        if (stmt instanceof Stmt.Return e) return e.keyword != null ? e.keyword.getLine() : 0;
        if (stmt instanceof Stmt.Switch e) return getLine(e.discriminant);
        if (stmt instanceof Stmt.Break e) return e.keyword != null ? e.keyword.getLine() : 0;
        if (stmt instanceof Stmt.Continue e) return e.keyword != null ? e.keyword.getLine() : 0;
        if (stmt instanceof Stmt.DestructuredVarDeclaration e) return getLine(e.pattern);
        if (stmt instanceof Stmt.ForIn e) {
            if (e.initializer != null) return getLine(e.initializer);
            return getLine(e.object);
        }
        return 0;
    }

    @Override
    public Void visitDestructuredVarDeclarationStmt(Stmt.DestructuredVarDeclaration stmt) {
        Object value = JSUndefined.INSTANCE;
        if (stmt.initializer != null) value = evaluate(stmt.initializer);
        boolean isConst = stmt.keyword.getType() == TokenType.CONST;
        destructure(stmt.pattern, value, isConst, true, this.environment);
        return null;
    }

    @Override
    public Object visitDestructuredAssignExpr(Expr.DestructuredAssign expr) {
        Object value = evaluate(expr.value);
        destructure(expr.pattern, value, false, false, this.environment);
        return value;
    }

    @Override
    public Object visitDeleteExpr(Expr.DeleteExpr expr) {
        if (expr.operand instanceof Expr.MemberAccess member) {
            Object obj = evaluate(member.object);
            if (obj == null || obj instanceof JSUndefined || obj instanceof JSNull) {
                throw new RuntimeError("TypeError: Cannot read properties of " + Stringify.stringify(obj), member.name);
            }
            if (obj instanceof Map<?, ?> map) {
                map.remove(member.name.getLexeme());
            }
            return true;
        } else if (expr.operand instanceof Expr.ComputedAccess computed) {
            Object obj = evaluate(computed.object);
            if (obj == null || obj instanceof JSUndefined || obj instanceof JSNull) {
                throw new RuntimeError("TypeError: Cannot read properties of " + Stringify.stringify(obj), computed.bracket);
            }
            Object index = evaluate(computed.index);
            if (obj instanceof ArrayList<?> list) {
                double idxD = toNumber(index);
                int idx = (int) idxD;
                if (idx >= 0 && idx < list.size()) {
                    ((List<Object>) list).set(idx, JSUndefined.INSTANCE);
                }
            } else if (obj instanceof Map<?, ?> map) {
                map.remove(getPropertyKey(index));
            }
            return true;
        }
        return false;
    }

    @Override
    public Object visitDefaultValExpr(Expr.DefaultVal expr) {
        Object val = evaluate(expr.target);
        if (val == JSUndefined.INSTANCE) {
            return evaluate(expr.defaultValue);
        }
        return val;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void visitForInStmt(Stmt.ForIn stmt) {
        explainEngine.explainFor(getLine(stmt));

        Object objVal = evaluate(stmt.object);
        if (objVal == null || objVal instanceof JSUndefined || objVal instanceof JSNull) {
            return null;
        }

        // Collect keys
        List<String> keys = new java.util.ArrayList<>();
        if (objVal instanceof Map<?, ?> map) {
            for (Object key : map.keySet()) {
                if (key instanceof String) {
                    keys.add((String) key);
                }
            }
        } else if (objVal instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                keys.add(String.valueOf(i));
            }
        } else if (objVal instanceof String s) {
            for (int i = 0; i < s.length(); i++) {
                keys.add(String.valueOf(i));
            }
        }

        Environment previous = this.environment;
        try {
            for (String key : keys) {
                // Each iteration gets its own block scope
                Environment iterationEnv = new Environment(previous);
                this.environment = iterationEnv;

                // Bind/assign the loop variable in iterationEnv
                assignLoopVariable(stmt.initializer, key, iterationEnv);

                try {
                    execute(stmt.body);
                } catch (BreakSignal e) {
                    break;
                } catch (ContinueSignal e) {
                    // continue to next iteration
                }
            }
        } finally {
            this.environment = previous;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void visitForOfStmt(Stmt.ForOf stmt) {
        explainEngine.explainFor(getLine(stmt));

        Object iterVal = evaluate(stmt.iterable);
        List<Object> elements = new ArrayList<>();

        if (iterVal instanceof List<?> list) {
            elements.addAll(list);
        } else if (iterVal instanceof String s) {
            for (int i = 0; i < s.length(); i++) {
                elements.add(String.valueOf(s.charAt(i)));
            }
        } else if (iterVal instanceof SetObject setObj) {
            elements.addAll(setObj.getElements());
        } else if (iterVal instanceof MapObject mapObj) {
            for (Map.Entry<Object, Object> entry : mapObj.getMap().entrySet()) {
                List<Object> pair = new ArrayList<>();
                pair.add(entry.getKey());
                pair.add(entry.getValue());
                elements.add(pair);
            }
        } else if (iterVal == null || iterVal instanceof JSUndefined || iterVal instanceof JSNull) {
            throw new RuntimeError("TypeError: " + Stringify.stringify(iterVal) + " is not iterable", getLine(stmt));
        } else {
            throw new RuntimeError("TypeError: " + Stringify.stringify(iterVal) + " is not iterable", getLine(stmt));
        }

        Environment previous = this.environment;
        try {
            for (Object element : elements) {
                // Each iteration gets its own block scope
                Environment iterationEnv = new Environment(previous);
                this.environment = iterationEnv;

                // Bind/assign the loop variable in iterationEnv
                assignLoopVariable(stmt.initializer, element, iterationEnv);

                try {
                    execute(stmt.body);
                } catch (BreakSignal e) {
                    break;
                } catch (ContinueSignal e) {
                    // continue to next iteration
                }
            }
        } finally {
            this.environment = previous;
        }
        return null;
    }

    private void assignLoopVariable(Stmt initializer, Object value, Environment env) {
        if (initializer instanceof Stmt.VarDeclaration varDecl) {
            boolean isConst = varDecl.keyword.getType() == TokenType.CONST;
            env.define(varDecl.name.getLexeme(), value, isConst);
        } else if (initializer instanceof Stmt.DestructuredVarDeclaration destDecl) {
            boolean isConst = destDecl.keyword.getType() == TokenType.CONST;
            destructure(destDecl.pattern, value, isConst, true, env);
        } else if (initializer instanceof Stmt.ExpressionStmt exprStmt) {
            Expr expr = exprStmt.expression;
            if (expr instanceof Expr.Identifier id) {
                // Assign to the existing variable in scope
                environment.assign(id.name.getLexeme(), value, id.name);
            } else if (expr instanceof Expr.ObjectLiteral || expr instanceof Expr.ArrayLiteral) {
                destructure(expr, value, false, false, env);
            } else {
                throw new RuntimeError("TypeError: Invalid loop variable", getLine(expr));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void destructure(Expr pattern, Object value, boolean isConst, boolean isDefine, Environment env) {
        if (pattern instanceof Expr.ObjectLiteral objLit) {
            if (value == null || value instanceof JSUndefined || value instanceof JSNull) {
                throw new RuntimeError("TypeError: Cannot destructure property of null or undefined", getLine(pattern));
            }
            
            Map<String, Object> map = null;
            if (value instanceof Map<?, ?>) {
                map = (Map<String, Object>) value;
            }

            java.util.Set<String> keysUsed = new java.util.HashSet<>();
            for (int i = 0; i < objLit.keys.size(); i++) {
                Object keyObj = objLit.keys.get(i);
                Expr valExpr = objLit.values.get(i);

                if (keyObj != null) {
                    String key;
                    if (keyObj instanceof Expr computedKeyExpr) {
                        key = Stringify.toJSString(evaluate(computedKeyExpr));
                    } else {
                        key = (String) keyObj;
                    }
                    keysUsed.add(key);

                    Object val = JSUndefined.INSTANCE;
                    if (map != null) {
                        val = map.getOrDefault(key, JSUndefined.INSTANCE);
                    } else {
                        val = getObjectProperty(value, key, getLine(pattern));
                    }

                    destructureTarget(valExpr, val, isConst, isDefine, env);
                } else {
                    if (valExpr instanceof Expr.Spread spread) {
                        LinkedHashMap<Object, Object> restMap = new LinkedHashMap<>();
                        if (value instanceof Map<?, ?> valMap) {
                            for (Map.Entry<?, ?> entry : valMap.entrySet()) {
                                Object k = entry.getKey();
                                String kStr = Stringify.toJSString(k);
                                if (!keysUsed.contains(kStr)) {
                                    restMap.put(k, entry.getValue());
                                }
                            }
                        }
                        destructureTarget(spread.expression, restMap, isConst, isDefine, env);
                    }
                }
            }
        } else if (pattern instanceof Expr.ArrayLiteral arrLit) {
            List<Object> list = null;
            if (value instanceof List<?>) {
                list = (List<Object>) value;
            } else if (value instanceof SetObject setObj) {
                list = new ArrayList<>(setObj.getElements());
            } else if (value instanceof MapObject mapObj) {
                list = new ArrayList<>();
                for (Map.Entry<Object, Object> entry : mapObj.getMap().entrySet()) {
                    ArrayList<Object> pair = new ArrayList<>();
                    pair.add(entry.getKey());
                    pair.add(entry.getValue());
                    list.add(pair);
                }
            } else if (value instanceof String s) {
                list = new ArrayList<>();
                for (int i = 0; i < s.length(); i++) {
                    list.add(String.valueOf(s.charAt(i)));
                }
            } else {
                throw new RuntimeError("TypeError: " + Stringify.stringify(value) + " is not iterable", getLine(pattern));
            }

            for (int i = 0; i < arrLit.elements.size(); i++) {
                Expr element = arrLit.elements.get(i);
                if (element == null) continue;

                if (element instanceof Expr.Spread spread) {
                    List<Object> restList = new ArrayList<>();
                    if (list != null && i < list.size()) {
                        restList.addAll(list.subList(i, list.size()));
                    }
                    destructureTarget(spread.expression, restList, isConst, isDefine, env);
                    break;
                }

                Object val = JSUndefined.INSTANCE;
                if (list != null && i < list.size()) {
                    val = list.get(i);
                }

                destructureTarget(element, val, isConst, isDefine, env);
            }
        } else {
            throw new RuntimeError("TypeError: Invalid destructuring pattern", getLine(pattern));
        }
    }

    private void destructureTarget(Expr target, Object value, boolean isConst, boolean isDefine, Environment env) {
        if (target instanceof Expr.DefaultVal defaultVal) {
            if (value == JSUndefined.INSTANCE) {
                value = evaluate(defaultVal.defaultValue);
            }
            target = defaultVal.target;
        }

        if (target instanceof Expr.Identifier id) {
            String name = id.name.getLexeme();
            if (isDefine) {
                env.define(name, value, isConst);
                explainEngine.explainVarDecl(id.name, value, getLine(id));
            } else {
                env.assign(name, value, id.name);
                explainEngine.explainAssign(id.name, value, getLine(id));
            }
        } else if (target instanceof Expr.ObjectLiteral || target instanceof Expr.ArrayLiteral) {
            destructure(target, value, isConst, isDefine, env);
        } else {
            throw new RuntimeError("TypeError: Invalid destructuring target", getLine(target));
        }
    }

    private Object getPropertyKey(Object index) {
        if (index instanceof JSSymbol) return index;
        return Stringify.toJSString(index);
    }

    @SuppressWarnings("unchecked")
    private List<Object> evaluateIterable(Object value, Token token) {
        if (value instanceof ArrayList<?> arr) {
            return (List<Object>) arr;
        }
        if (value instanceof SetObject setObj) {
            return new ArrayList<>(setObj.getElements());
        }
        if (value instanceof MapObject mapObj) {
            List<Object> entries = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : mapObj.getMap().entrySet()) {
                ArrayList<Object> pair = new ArrayList<>();
                pair.add(entry.getKey());
                pair.add(entry.getValue());
                entries.add(pair);
            }
            return entries;
        }
        if (value instanceof String s) {
            List<Object> chars = new ArrayList<>();
            for (int i = 0; i < s.length(); i++) {
                chars.add(String.valueOf(s.charAt(i)));
            }
            return chars;
        }
        throw new RuntimeError("Cannot spread non-iterable", token);
    }

    private Object getObjectProperty(Object object, String prop, int line) {
        try {
            return evaluateMemberAccess(object, prop, null);
        } catch (Exception e) {
            return JSUndefined.INSTANCE;
        }
    }
}
