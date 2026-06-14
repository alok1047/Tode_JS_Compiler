package thunderjs.runtime;

public class StackFrame {
    public final String functionName;
    public int line;
    public int column;
    
    public StackFrame(String functionName, int line, int column) {
        this.functionName = functionName;
        this.line = line;
        this.column = column;
    }
}
