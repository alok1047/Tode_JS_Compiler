package thunderjs.runtime;

public class SourceSnippet {
    // Extract line from full source text (1-based line number)
    public static String getLine(String source, int lineNumber) {
        if (source == null || lineNumber <= 0) return "";
        String[] lines = source.split("\\r?\\n", -1);
        if (lineNumber > lines.length) return "";
        return lines[lineNumber - 1];
    }
    
    // Build caret pointer: "               ^^^^^^^"
    public static String buildCaret(int column, int length) {
        if (column <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < column; i++) {
            sb.append(" ");
        }
        int len = length > 0 ? length : 1;
        for (int i = 0; i < len; i++) {
            sb.append("^");
        }
        return sb.toString();
    }
}
