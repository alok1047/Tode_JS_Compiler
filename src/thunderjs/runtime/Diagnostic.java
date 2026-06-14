package thunderjs.runtime;

import java.util.List;

public class Diagnostic {
    public enum Severity { ERROR, WARNING }
    
    public final Severity severity;
    public final String errorType;     // "SyntaxError", "ReferenceError", "TypeError"
    public final String message;       // "usernme is not defined"
    public final String fileName;      // "login.js"
    public final int line;             // 18
    public final int column;           // 15
    public final int caretLength;      // length of caret pointer
    public final String sourceCode;    // full source text
    public final String suggestion;    // "username" (nullable)
    public final List<StackFrame> callStack;  // (nullable)

    private Diagnostic(Builder builder) {
        this.severity = builder.severity;
        this.errorType = builder.errorType;
        this.message = builder.message;
        this.fileName = builder.fileName;
        this.line = builder.line;
        this.column = builder.column;
        this.caretLength = builder.caretLength;
        this.sourceCode = builder.sourceCode;
        this.suggestion = builder.suggestion;
        this.callStack = builder.callStack;
    }

    public static Builder error(String errorType, String message) {
        return new Builder(Severity.ERROR, errorType, message);
    }

    public static class Builder {
        private final Severity severity;
        private final String errorType;
        private final String message;
        private String fileName = "input.js";
        private int line = -1;
        private int column = -1;
        private int caretLength = 1;
        private String sourceCode = null;
        private String suggestion = null;
        private List<StackFrame> callStack = null;

        public Builder(Severity severity, String errorType, String message) {
            this.severity = severity;
            this.errorType = errorType;
            this.message = message;
        }

        public Builder at(String fileName, int line, int column) {
            this.fileName = fileName != null ? fileName : "input.js";
            this.line = line;
            this.column = column;
            return this;
        }

        public Builder withCaretLength(int caretLength) {
            this.caretLength = caretLength;
            return this;
        }

        public Builder withSource(String sourceCode) {
            this.sourceCode = sourceCode;
            return this;
        }

        public Builder withSuggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public Builder withCallStack(List<StackFrame> callStack) {
            this.callStack = callStack;
            return this;
        }

        public Diagnostic build() {
            return new Diagnostic(this);
        }
    }
}
