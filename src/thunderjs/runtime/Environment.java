package thunderjs.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * Lexically-scoped variable environment for ThunderJS.
 *
 * Each environment has:
 *   - A map of variable names → values
 *   - An optional parent (enclosing) environment
 *   - Whether it tracks const declarations
 *
 * Variable lookup walks up the parent chain (lexical scoping).
 * Block statements, function bodies, and loop bodies each create
 * a new child environment.
 */
public class Environment {

    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, Boolean> constants = new HashMap<>();
    private final Environment parent;

    /** Create the global environment (no parent). */
    public Environment() {
        this.parent = null;
    }

    /** Create a child (enclosed) environment. */
    public Environment(Environment parent) {
        this.parent = parent;
    }

    // ── Variable operations ─────────────────────────────────────────────

    /**
     * Define a new variable in the current scope.
     *
     * @param name       variable name
     * @param value      initial value
     * @param isConstant true if declared with 'const'
     */
    public void define(String name, Object value, boolean isConstant) {
        values.put(name, value);
        if (isConstant) {
            constants.put(name, true);
        }
    }

    /**
     * Define a mutable variable (let or var).
     */
    public void define(String name, Object value) {
        define(name, value, false);
    }

    /**
     * Get the value of a variable by name.
     * Walks up the parent chain until found.
     *
     * @throws RuntimeError if the variable is not defined anywhere
     */
    public Object get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (parent != null) {
            return parent.get(name);
        }
        throw new RuntimeError("ReferenceError: " + name + " is not defined");
    }

    /**
     * Check if a variable is defined in this scope or any parent.
     */
    public boolean has(String name) {
        if (values.containsKey(name)) return true;
        if (parent != null) return parent.has(name);
        return false;
    }

    /**
     * Assign a new value to an existing variable.
     * Walks up the parent chain to find the variable.
     *
     * @throws RuntimeError if the variable is not defined, or is const
     */
    public void assign(String name, Object value) {
        if (values.containsKey(name)) {
            if (constants.containsKey(name)) {
                throw new RuntimeError("TypeError: Assignment to constant variable '" + name + "'");
            }
            values.put(name, value);
            return;
        }
        if (parent != null) {
            parent.assign(name, value);
            return;
        }
        throw new RuntimeError("ReferenceError: " + name + " is not defined");
    }

    /**
     * Get the parent environment.
     */
    public Environment getParent() {
        return parent;
    }

    /**
     * Get all variables in the current scope (not parents).
     * Useful for debug/trace output.
     */
    public Map<String, Object> getLocalValues() {
        return new HashMap<>(values);
    }
}
