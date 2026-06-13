package thunderjs.interpreter;

import thunderjs.ast.Expr;
import thunderjs.ast.Stmt;
import thunderjs.builtins.ConsoleObject;
import thunderjs.builtins.MathObject;
import thunderjs.lexer.Token;
import thunderjs.lexer.TokenType;
import thunderjs.runtime.*;
import thunderjs.util.Stringify;

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
    private thunderjs.debugger.CoverageTracker coverageTracker = new thunderjs.debugger.CoverageTracker(false);
    private thunderjs.debugger.TraceEngine traceEngine = new thunderjs.debugger.TraceEngine(false);
    private thunderjs.debugger.ExplainEngine explainEngine = new thunderjs.debugger.ExplainEngine(false);

    public void setCoverageTracker(thunderjs.debugger.CoverageTracker tracker) { this.coverageTracker = tracker; }
    public void setTraceEngine(thunderjs.debugger.TraceEngine engine) { this.traceEngine = engine; }
    public void setExplainEngine(thunderjs.debugger.ExplainEngine engine) { this.explainEngine = engine; }

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
        for (String key : List.of("keys", "values", "entries")) {
            objectObj.put(key, getObjectStaticMethod(key));
        }
        globals.define("Object", objectObj);
        globals.define("Date", new DateConstructor());
    }

    // ── Public API ──────────────────────────────────────────────────────

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } catch (RuntimeError e) {
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
        Environment previous = this.environment;
        try {
            this.environment = env;
            for (Stmt stmt : statements) execute(stmt);
        } finally {
            this.environment = previous;
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
        return environment.get(expr.name.getLexeme());
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
            default -> throw new RuntimeError("Unknown operator: " + expr.operator.getLexeme(), expr.operator.getLine());
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
            default -> throw new RuntimeError("Unknown unary op: " + expr.operator.getLexeme(),
                    expr.operator.getLine());
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
        environment.assign(expr.name.getLexeme(), value);
        explainEngine.explainAssign(expr.name, value, getLine(expr));
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitMemberAssignExpr(Expr.MemberAssign expr) {
        Object obj = evaluate(expr.object);
        Object value = evaluate(expr.value);
        if (obj instanceof LinkedHashMap) {
            ((Map<String, Object>) obj).put(expr.name.getLexeme(), value);
            return value;
        }
        throw new RuntimeError("Cannot set property '" + expr.name.getLexeme() + "' of " +
                Stringify.stringify(obj), expr.name.getLine());
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
            ((Map<String, Object>) obj).put(Stringify.toJSString(index), value);
            return value;
        }
        throw new RuntimeError("Cannot set property of " + Stringify.stringify(obj),
                expr.bracket.getLine());
    }

    @Override
    public Object visitCompoundAssignExpr(Expr.CompoundAssign expr) {
        Object current = environment.get(expr.name.getLexeme());
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
            default -> throw new RuntimeError("Unknown compound op: " + expr.operator.getLexeme());
        };
        environment.assign(expr.name.getLexeme(), result);
        return result;
    }

    @Override
    public Object visitUpdateExpr(Expr.Update expr) {
        Object current = environment.get(expr.name.getLexeme());
        double val = toNumber(current);
        double newVal = expr.operator.getType() == TokenType.PLUS_PLUS ? val + 1 : val - 1;
        environment.assign(expr.name.getLexeme(), newVal);
        return expr.isPrefix ? newVal : val;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        // Evaluate arguments (handle spread)
        List<Object> args = new ArrayList<>();
        for (Expr arg : expr.arguments) {
            if (arg instanceof Expr.Spread spread) {
                Object spreadVal = evaluate(spread.expression);
                if (spreadVal instanceof ArrayList<?> arr) {
                    args.addAll((List<Object>) arr);
                } else {
                    throw new RuntimeError("Cannot spread non-iterable", expr.paren.getLine());
                }
            } else {
                args.add(evaluate(arg));
            }
        }

        if (callee instanceof JSFunction fn) return callFunction(fn, args);
        if (callee instanceof JSCallable callable) {
            if (isConsoleLog(expr)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < args.size(); i++) {
                    sb.append(Stringify.stringify(args.get(i)));
                    if (i < args.size() - 1) sb.append(" ");
                }
                explainEngine.explainPrint(sb.toString(), expr.paren.getLine());
            } else {
                String calleeName = thunderjs.debugger.ASTPrinter.SourceCodeReconstructor.toSource(expr.callee);
                explainEngine.explainCall(calleeName, expr.paren.getLine());
            }
            return callable.call(this, args);
        }

        throw new RuntimeError("TypeError: " + Stringify.stringify(callee) + " is not a function",
                expr.paren.getLine());
    }

    @Override
    public Object visitNewExpr(Expr.New expr) {
        Object constructor = evaluate(expr.constructor);

        List<Object> args = new ArrayList<>();
        if (expr.arguments != null) {
            for (Expr arg : expr.arguments) {
                if (arg instanceof Expr.Spread spread) {
                    Object spreadVal = evaluate(spread.expression);
                    if (spreadVal instanceof ArrayList<?> arr) {
                        args.addAll((List<Object>) arr);
                    } else {
                        throw new RuntimeError("Cannot spread non-iterable", expr.keyword.getLine());
                    }
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

        throw new RuntimeError("TypeError: " + Stringify.stringify(constructor) + " is not a constructor",
                expr.keyword.getLine());
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
        int line = fn.getParams().isEmpty() ? 0 : fn.getParams().get(0).name.getLine();
        explainEngine.explainCall(fn.name(), line);
        traceEngine.push(fn.name(), line);

        try {
            Environment funcEnv = new Environment(fn.getClosure());

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
                    return evaluate(fn.getConciseBody());
                } finally { this.environment = previous; }
            }

            try {
                executeBlock(fn.getBody(), funcEnv);
            } catch (ReturnSignal ret) {
                return ret.value;
            }
            return JSUndefined.INSTANCE;
        } finally {
            traceEngine.pop();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitMemberAccessExpr(Expr.MemberAccess expr) {
        Object object = evaluate(expr.object);
        String prop = expr.name.getLexeme();

        if (object instanceof DateObject dateObj) {
            return dateObj.getProperty(prop, expr.name.getLine());
        }
        if (object instanceof DateConstructor dateConst) {
            return dateConst.getProperty(prop);
        }

        // ── String properties/methods ───────────────────────────────
        if (object instanceof String s) return getStringProperty(s, prop);

        // ── Array properties/methods ────────────────────────────────
        if (object instanceof ArrayList<?> arr) return getArrayProperty((List<Object>) arr, prop);

        // ── Object properties ───────────────────────────────────────
        if (object instanceof LinkedHashMap<?, ?> map) {
            Object val = ((Map<String, Object>) map).get(prop);
            return val != null ? val : JSUndefined.INSTANCE;
        }

        throw new RuntimeError("TypeError: Cannot read property '" + prop + "' of " +
                Stringify.stringify(object), expr.name.getLine());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitComputedAccessExpr(Expr.ComputedAccess expr) {
        Object object = evaluate(expr.object);
        Object index = evaluate(expr.index);

        if (object instanceof DateObject dateObj) {
            String prop = Stringify.toJSString(index);
            return dateObj.getProperty(prop, expr.bracket.getLine());
        }
        if (object instanceof DateConstructor dateConst) {
            String prop = Stringify.toJSString(index);
            return dateConst.getProperty(prop);
        }

        if (object instanceof ArrayList<?> arr) {
            int idx = (int) toNumber(index);
            List<Object> list = (List<Object>) arr;
            if (idx < 0 || idx >= list.size()) return JSUndefined.INSTANCE;
            return list.get(idx);
        }
        if (object instanceof LinkedHashMap<?, ?> map) {
            Object val = ((Map<String, Object>) map).get(Stringify.toJSString(index));
            return val != null ? val : JSUndefined.INSTANCE;
        }
        if (object instanceof String s) {
            int idx = (int) toNumber(index);
            if (idx < 0 || idx >= s.length()) return JSUndefined.INSTANCE;
            return String.valueOf(s.charAt(idx));
        }
        throw new RuntimeError("TypeError: Cannot read property of " + Stringify.stringify(object),
                expr.bracket.getLine());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitArrayLiteralExpr(Expr.ArrayLiteral expr) {
        List<Object> elements = new ArrayList<>();
        for (Expr el : expr.elements) {
            if (el instanceof Expr.Spread spread) {
                Object sv = evaluate(spread.expression);
                if (sv instanceof ArrayList<?> arr) elements.addAll((List<Object>) arr);
                else throw new RuntimeError("Cannot spread non-iterable", expr.bracket.getLine());
            } else {
                elements.add(evaluate(el));
            }
        }
        return elements;
    }

    @Override
    public Object visitObjectLiteralExpr(Expr.ObjectLiteral expr) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < expr.keys.size(); i++)
            map.put(expr.keys.get(i), evaluate(expr.values.get(i)));
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
                if (a.get(0) instanceof LinkedHashMap<?, ?> map)
                    return new ArrayList<>(((Map<String, Object>) map).keySet());
                return new ArrayList<>();
            };
            case "values" -> (JSCallable) (i, a) -> {
                if (a.get(0) instanceof LinkedHashMap<?, ?> map)
                    return new ArrayList<>(((Map<String, Object>) map).values());
                return new ArrayList<>();
            };
            case "entries" -> (JSCallable) (i, a) -> {
                if (a.get(0) instanceof LinkedHashMap<?, ?> map) {
                    List<Object> entries = new ArrayList<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        List<Object> pair = new ArrayList<>();
                        pair.add(entry.getKey()); pair.add(entry.getValue());
                        entries.add(pair);
                    }
                    return entries;
                }
                return new ArrayList<>();
            };
            default -> throw new RuntimeError("TypeError: Object." + name + " is not a function");
        };
    }

    // ── String methods ──────────────────────────────────────────────────

    private Object getStringProperty(String s, String prop) {
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
                    "TypeError: \"" + s + "\"." + prop + " is not a function");
        };
    }

    // ── Array methods ───────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Object getArrayProperty(List<Object> arr, String prop) {
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
                } else if (a.get(0) instanceof JSFunction fn) {
                    arr.sort((x, y) -> {
                        List<Object> cmpArgs = new ArrayList<>(); cmpArgs.add(x); cmpArgs.add(y);
                        return (int) toNumber(callFunction(fn, cmpArgs));
                    });
                }
                return arr;
            };
            case "toString" -> (JSCallable) (i, a) -> Stringify.stringify(arr);

            // ── Higher-order methods ────────────────────────────────
            case "map" -> (JSCallable) (i, a) -> {
                JSFunction fn = (JSFunction) a.get(0);
                List<Object> result = new ArrayList<>();
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cb = new ArrayList<>(); cb.add(arr.get(j)); cb.add((double)j); cb.add(arr);
                    result.add(callFunction(fn, cb));
                }
                return result;
            };
            case "filter" -> (JSCallable) (i, a) -> {
                JSFunction fn = (JSFunction) a.get(0);
                List<Object> result = new ArrayList<>();
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cb = new ArrayList<>(); cb.add(arr.get(j)); cb.add((double)j); cb.add(arr);
                    if (Stringify.isTruthy(callFunction(fn, cb))) result.add(arr.get(j));
                }
                return result;
            };
            case "reduce" -> (JSCallable) (i, a) -> {
                JSFunction fn = (JSFunction) a.get(0);
                Object acc; int startIdx;
                if (a.size() > 1) { acc = a.get(1); startIdx = 0; }
                else { if (arr.isEmpty()) throw new RuntimeError("Reduce of empty array with no initial value"); acc = arr.get(0); startIdx = 1; }
                for (int j = startIdx; j < arr.size(); j++) {
                    List<Object> cb = new ArrayList<>(); cb.add(acc); cb.add(arr.get(j)); cb.add((double)j); cb.add(arr);
                    acc = callFunction(fn, cb);
                }
                return acc;
            };
            case "find" -> (JSCallable) (i, a) -> {
                JSFunction fn = (JSFunction) a.get(0);
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cb = new ArrayList<>(); cb.add(arr.get(j)); cb.add((double)j);
                    if (Stringify.isTruthy(callFunction(fn, cb))) return arr.get(j);
                }
                return JSUndefined.INSTANCE;
            };
            case "findIndex" -> (JSCallable) (i, a) -> {
                JSFunction fn = (JSFunction) a.get(0);
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cb = new ArrayList<>(); cb.add(arr.get(j)); cb.add((double)j);
                    if (Stringify.isTruthy(callFunction(fn, cb))) return (double) j;
                }
                return -1.0;
            };
            case "some" -> (JSCallable) (i, a) -> {
                JSFunction fn = (JSFunction) a.get(0);
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cb = new ArrayList<>(); cb.add(arr.get(j)); cb.add((double)j);
                    if (Stringify.isTruthy(callFunction(fn, cb))) return true;
                }
                return false;
            };
            case "every" -> (JSCallable) (i, a) -> {
                JSFunction fn = (JSFunction) a.get(0);
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cb = new ArrayList<>(); cb.add(arr.get(j)); cb.add((double)j);
                    if (!Stringify.isTruthy(callFunction(fn, cb))) return false;
                }
                return true;
            };
            case "forEach" -> (JSCallable) (i, a) -> {
                JSFunction fn = (JSFunction) a.get(0);
                for (int j = 0; j < arr.size(); j++) {
                    List<Object> cb = new ArrayList<>(); cb.add(arr.get(j)); cb.add((double)j); cb.add(arr);
                    callFunction(fn, cb);
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
            default -> throw new RuntimeError("TypeError: arr." + prop + " is not a function");
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
        if (expr instanceof Expr.Update e) return e.name != null ? e.name.getLine() : 0;
        if (expr instanceof Expr.Call e) return e.paren != null ? e.paren.getLine() : 0;
        if (expr instanceof Expr.MemberAccess e) return e.name != null ? e.name.getLine() : 0;
        if (expr instanceof Expr.ComputedAccess e) return e.bracket != null ? e.bracket.getLine() : 0;
        if (expr instanceof Expr.Ternary e) return getLine(e.condition);
        if (expr instanceof Expr.TemplateLiteral e) return e.token != null ? e.token.getLine() : 0;
        if (expr instanceof Expr.Grouping e) return getLine(e.expression);
        if (expr instanceof Expr.TypeofExpr e) return e.keyword != null ? e.keyword.getLine() : 0;
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
        return 0;
    }
}
