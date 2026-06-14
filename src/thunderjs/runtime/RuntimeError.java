package thunderjs.runtime;

import thunderjs.lexer.Token;

/**
 * Custom runtime error for ThunderJS.
 *
 * Carries line, column, token, and suggestion details for structured error reporting.
 */
public class RuntimeError extends RuntimeException {

    private final int line;
    private final int column;
    private final Token token;
    private final String suggestion;
    private java.util.List<StackFrame> callStack;

    public RuntimeError(String message, int line) {
        super(message);
        this.line = line;
        this.column = -1;
        this.token = null;
        this.suggestion = null;
        this.callStack = null;
    }

    public RuntimeError(String message) {
        this(message, -1);
    }

    public RuntimeError(String message, Token token) {
        super(message);
        this.token = token;
        this.line = token != null ? token.getLine() : -1;
        this.column = token != null ? token.getColumn() : -1;
        this.suggestion = null;
        this.callStack = null;
    }

    public RuntimeError(String message, Token token, String suggestion) {
        super(message);
        this.token = token;
        this.line = token != null ? token.getLine() : -1;
        this.column = token != null ? token.getColumn() : -1;
        this.suggestion = suggestion;
        this.callStack = null;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public Token getToken() {
        return token;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public java.util.List<StackFrame> getCallStack() {
        return callStack;
    }

    public void setCallStack(java.util.List<StackFrame> callStack) {
        this.callStack = callStack;
    }

    @Override
    public String toString() {
        if (line > 0) {
            return getMessage() + "\n    at line " + line;
        }
        return getMessage();
    }
}
