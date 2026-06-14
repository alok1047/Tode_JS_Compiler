package thunderjs.runtime;

public class DiagnosticFormatter {
    public static String format(Diagnostic diag) {
        StringBuilder sb = new StringBuilder();
        // 1. Error type + Message
        sb.append("\u001B[31m").append(diag.errorType).append(": ").append(diag.message).append("\u001B[0m\n");
        // 2. File
        sb.append("  File: ").append(diag.fileName).append("\n");
        
        // 3. Line, Column
        int line = diag.line > 0 ? diag.line : 1;
        int column = diag.column > 0 ? diag.column : 1;
        sb.append("  Line: ").append(line).append(", Column: ").append(column).append("\n\n");
        
        // 4. Source snippet
        if (diag.sourceCode != null && !diag.sourceCode.isEmpty()) {
            String sourceLine = SourceSnippet.getLine(diag.sourceCode, line);
            // Prefix line number gutter
            sb.append("  ").append(line).append(" | ").append(sourceLine).append("\n");
            // 5. Caret pointer
            String caret = SourceSnippet.buildCaret(column, diag.caretLength);
            // Gutter spacing is (length of line number string) + 3 spaces for " | "
            int gutterWidth = String.valueOf(line).length() + 3;
            sb.append("  ");
            for (int i = 0; i < gutterWidth; i++) {
                sb.append(" ");
            }
            sb.append("\u001B[31m").append(caret).append("\u001B[0m\n");
        } else {
            // If source code is not available
            sb.append("  1 | <source unavailable>\n");
            sb.append("      \u001B[31m^\u001B[0m\n");
        }
        
        // 6. Suggestion (if present)
        if (diag.suggestion != null && !diag.suggestion.isEmpty()) {
            sb.append("\n  💡 \u001B[32mDid you mean: ").append(diag.suggestion).append("\u001B[0m\n");
        }

        // 7. Stack trace (if present)
        if (diag.callStack != null && !diag.callStack.isEmpty()) {
            sb.append("\n  Call Stack:\n");
            for (int i = diag.callStack.size() - 1; i >= 0; i--) {
                StackFrame frame = diag.callStack.get(i);
                sb.append("    at ").append(frame.functionName)
                  .append("()   line ").append(frame.line)
                  .append("\n");
            }
        }
        
        return sb.toString();
    }
}
