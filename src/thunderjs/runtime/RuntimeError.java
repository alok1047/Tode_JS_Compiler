package thunderjs.runtime;

/**
 * Custom runtime error for ThunderJS.
 *
 * Carries a line number for error reporting in a Node.js-like format.
 */
public class RuntimeError extends RuntimeException {

    private final int line;

    public RuntimeError(String message, int line) {
        super(message);
        this.line = line;
    }

    public RuntimeError(String message) {
        this(message, -1);
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        if (line > 0) {
            return getMessage() + "\n    at line " + line;
        }
        return getMessage();
    }
}
