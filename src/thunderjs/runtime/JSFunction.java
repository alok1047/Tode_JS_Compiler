package thunderjs.runtime;

import thunderjs.ast.Expr;
import thunderjs.ast.Stmt;

import java.util.List;

/**
 * Represents a user-defined JavaScript function.
 *
 * Stores:
 *   - The function name (may be null for anonymous)
 *   - The parameter list
 *   - The function body (list of statements)
 *   - The closure environment (the environment at the time of definition)
 *
 * This is the key class for supporting closures: when a function is called,
 * its body executes in a new environment whose parent is the *closure*
 * environment, NOT the calling environment.
 */
public class JSFunction implements JSCallable {

    private final String functionName;
    private final List<Expr.Parameter> params;
    private final List<Stmt> body;
    private final Expr conciseBody;       // for arrow functions with expression body
    private final Environment closure;

    /**
     * Create a function with a block body.
     */
    public JSFunction(String name, List<Expr.Parameter> params,
                      List<Stmt> body, Environment closure) {
        this.functionName = name;
        this.params = params;
        this.body = body;
        this.conciseBody = null;
        this.closure = closure;
    }

    /**
     * Create an arrow function with a concise (expression) body.
     */
    public JSFunction(String name, List<Expr.Parameter> params,
                      Expr conciseBody, Environment closure) {
        this.functionName = name;
        this.params = params;
        this.body = null;
        this.conciseBody = conciseBody;
        this.closure = closure;
    }

    public List<Expr.Parameter> getParams() {
        return params;
    }

    public List<Stmt> getBody() {
        return body;
    }

    public Expr getConciseBody() {
        return conciseBody;
    }

    public Environment getClosure() {
        return closure;
    }

    public boolean hasConciseBody() {
        return conciseBody != null;
    }

    @Override
    public Object call(Object interpreter, List<Object> arguments) {
        // Actual call logic is in Interpreter.java — this is just data storage.
        // The Interpreter handles binding params, creating scope, and executing body.
        throw new UnsupportedOperationException(
            "JSFunction.call() should be invoked through the Interpreter");
    }

    @Override
    public int arity() {
        // Count non-rest parameters
        int count = 0;
        for (Expr.Parameter p : params) {
            if (!p.isRest) count++;
        }
        return count;
    }

    @Override
    public String name() {
        return functionName != null ? functionName : "<anonymous>";
    }

    @Override
    public String toString() {
        return "[Function: " + name() + "]";
    }
}
