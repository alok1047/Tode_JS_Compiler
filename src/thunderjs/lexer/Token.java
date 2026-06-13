package thunderjs.lexer;

/**
 * Represents a single lexical token produced by the {@link Lexer}.
 *
 * Every token carries:
 *   - {@code type}    – the semantic category (see {@link TokenType})
 *   - {@code lexeme}  – the raw source text that was matched
 *   - {@code literal} – the computed value for literals (Double for numbers,
 *                        String for strings, null otherwise)
 *   - {@code line}    – 1-based line number in the source file
 *   - {@code column}  – 1-based column number where the token starts
 */
public class Token {

    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final int line;
    private final int column;

    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    /** Convenience constructor for tokens without a literal value. */
    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, null, line, column);
    }

    // ── Accessors ───────────────────────────────────────────────────────

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    // ── Display ─────────────────────────────────────────────────────────

    @Override
    public String toString() {
        if (literal != null) {
            return String.format("[%s] '%s' = %s  (line %d, col %d)",
                    type, lexeme, literal, line, column);
        }
        return String.format("[%s] '%s'  (line %d, col %d)",
                type, lexeme, line, column);
    }
}
