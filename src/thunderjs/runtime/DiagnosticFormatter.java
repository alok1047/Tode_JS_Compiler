package thunderjs.runtime;

import thunderjs.util.Ansi;

public class DiagnosticFormatter {
    public static String format(Diagnostic diag) {
        StringBuilder sb = new StringBuilder();
        
        // 1. Error type + Message
        sb.append(Ansi.red("❌ " + diag.errorType + ": " + diag.message)).append("\n\n");
        
        // 2. File
        sb.append(Ansi.yellow("File: " + diag.fileName)).append("\n");
        
        // 3. Line, Column
        int line = diag.line > 0 ? diag.line : 1;
        int column = diag.column > 0 ? diag.column : 1;
        sb.append(Ansi.yellow("Line: " + line)).append("\n");
        sb.append(Ansi.yellow("Column: " + column)).append("\n\n");
        
        // 4. Source snippet
        if (diag.sourceCode != null && !diag.sourceCode.isEmpty()) {
            String sourceLine = SourceSnippet.getLine(diag.sourceCode, line);
            // Prefix line number gutter
            sb.append(line).append(" | ").append(sourceLine).append("\n");
            // 5. Caret pointer
            String caret = SourceSnippet.buildCaret(column, diag.caretLength);
            // Gutter spacing is (length of line number string) + 3 spaces for " | "
            int gutterWidth = String.valueOf(line).length() + 3;
            for (int i = 0; i < gutterWidth; i++) {
                sb.append(" ");
            }
            sb.append(Ansi.red(caret)).append("\n\n");
        } else {
            // If source code is not available
            sb.append("1 | <source unavailable>\n");
            sb.append("    ").append(Ansi.red("^")).append("\n\n");
        }
        
        // 6. Suggestion (if present)
        if (diag.suggestion != null && !diag.suggestion.isEmpty()) {
            sb.append(Ansi.green("💡 Did you mean:")).append("\n");
            sb.append(Ansi.green("   " + diag.suggestion)).append("\n");
        }

        // 7. Stack trace (if present)
        if (diag.callStack != null && !diag.callStack.isEmpty()) {
            sb.append("\n").append(Ansi.blue("Call Stack:")).append("\n");
            for (int i = diag.callStack.size() - 1; i >= 0; i--) {
                StackFrame frame = diag.callStack.get(i);
                sb.append("  at ").append(frame.functionName)
                  .append("()   line ").append(frame.line)
                  .append("\n");
            }
        }
        
        return sb.toString();
    }
}
