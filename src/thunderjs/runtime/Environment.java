package thunderjs.runtime;

import thunderjs.lexer.Token;
import thunderjs.util.SuggestionEngine;
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
        String closest = SuggestionEngine.suggestVariable(name, this);
        if (closest != null) {
            throw new RuntimeError("ReferenceError: " + name + " is not defined\n\n💡 Did you mean:\n   " + closest);
        }
        throw new RuntimeError("ReferenceError: " + name + " is not defined");
    }

    /**
     * Get the value of a variable by name, using a Token for diagnostics.
     */
    public Object get(String name, Token token) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (parent != null) {
            return parent.get(name, token);
        }
        String closest = SuggestionEngine.suggestVariable(name, this);
        if (closest != null) {
            throw new RuntimeError("ReferenceError: " + name + " is not defined", token, closest);
        }
        throw new RuntimeError("ReferenceError: " + name + " is not defined", token);
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
        String closest = SuggestionEngine.suggestVariable(name, this);
        if (closest != null) {
            throw new RuntimeError("ReferenceError: " + name + " is not defined\n\n💡 Did you mean:\n   " + closest);
        }
        throw new RuntimeError("ReferenceError: " + name + " is not defined");
    }

    /**
     * Assign a new value to an existing variable, using a Token for diagnostics.
     */
    public void assign(String name, Object value, Token token) {
        if (values.containsKey(name)) {
            if (constants.containsKey(name)) {
                throw new RuntimeError("TypeError: Assignment to constant variable '" + name + "'", token);
            }
            values.put(name, value);
            return;
        }
        if (parent != null) {
            parent.assign(name, value, token);
            return;
        }
        String closest = SuggestionEngine.suggestVariable(name, this);
        if (closest != null) {
            throw new RuntimeError("ReferenceError: " + name + " is not defined", token, closest);
        }
        throw new RuntimeError("ReferenceError: " + name + " is not defined", token);
    }

    /**
     * Get all variable names visible in the current environment and parent scope chains.
     */
    public java.util.Set<String> getAllVisibleNames() {
        java.util.Set<String> names = new java.util.HashSet<>(values.keySet());
        if (parent != null) {
            names.addAll(parent.getAllVisibleNames());
        }
        return names;
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
