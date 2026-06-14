package thunderjs.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lexer (scanner) for ThunderJS.
 *
 * Converts raw JavaScript source text into a list of {@link Token}s.
 * Handles:
 *   - Single / double-quoted strings
 *   - Template literals with ${...} interpolation (emitted as TEMPLATE_LITERAL tokens)
 *   - Number literals (integer and floating-point)
 *   - All JavaScript keywords
 *   - All operators including multi-character ones (===, !==, **, =>, ...)
 *   - Single-line (//) and multi-line (/* ... *​/) comments
 *   - Line and column tracking for error reporting
 */
public class Lexer {

    // ── Keyword table ───────────────────────────────────────────────────
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("let",       TokenType.LET);
        KEYWORDS.put("const",     TokenType.CONST);
        KEYWORDS.put("var",       TokenType.VAR);
        KEYWORDS.put("if",        TokenType.IF);
        KEYWORDS.put("else",      TokenType.ELSE);
        KEYWORDS.put("for",       TokenType.FOR);
        KEYWORDS.put("while",     TokenType.WHILE);
        KEYWORDS.put("do",        TokenType.DO);
        KEYWORDS.put("switch",    TokenType.SWITCH);
        KEYWORDS.put("case",      TokenType.CASE);
        KEYWORDS.put("default",   TokenType.DEFAULT);
        KEYWORDS.put("break",     TokenType.BREAK);
        KEYWORDS.put("continue",  TokenType.CONTINUE);
        KEYWORDS.put("function",  TokenType.FUNCTION);
        KEYWORDS.put("return",    TokenType.RETURN);
        KEYWORDS.put("true",      TokenType.TRUE);
        KEYWORDS.put("false",     TokenType.FALSE);
        KEYWORDS.put("null",      TokenType.NULL);
        KEYWORDS.put("undefined", TokenType.UNDEFINED);
        KEYWORDS.put("typeof",    TokenType.TYPEOF);
        KEYWORDS.put("new",       TokenType.NEW);
        KEYWORDS.put("in",        TokenType.IN);
        KEYWORDS.put("delete",    TokenType.DELETE);
    }

    // ── Instance state ──────────────────────────────────────────────────
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start   = 0;   // start of the current lexeme
    private int current = 0;   // current scan position
    private int line    = 1;   // current line  (1-based)
    private int column  = 1;   // current column (1-based)
    private int startColumn = 1;

    // ── Constructor ─────────────────────────────────────────────────────

    public Lexer(String source) {
        this.source = source;
    }

    // ── Public API ──────────────────────────────────────────────────────

    /**
     * Scan the entire source and return the list of tokens
     * (always terminated by an EOF token).
     */
    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            startColumn = column;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line, column));
        return tokens;
    }

    // ── Core scanner ────────────────────────────────────────────────────

    private void scanToken() {
        char c = advance();

        switch (c) {
            // ── Single-character delimiters ──────────────────────────
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case '[' -> addToken(TokenType.LEFT_BRACKET);
            case ']' -> addToken(TokenType.RIGHT_BRACKET);
            case ';' -> addToken(TokenType.SEMICOLON);
            case ',' -> addToken(TokenType.COMMA);
            case ':' -> addToken(TokenType.COLON);
            case '?' -> addToken(TokenType.QUESTION);

            // ── Dot / spread ────────────────────────────────────────
            case '.' -> {
                if (peek() == '.' && peekNext() == '.') {
                    advance(); // consume second '.'
                    advance(); // consume third '.'
                    addToken(TokenType.DOT_DOT_DOT);
                } else {
                    addToken(TokenType.DOT);
                }
            }

            // ── Arithmetic operators ────────────────────────────────
            case '+' -> {
                if (match('+'))      addToken(TokenType.PLUS_PLUS);
                else if (match('=')) addToken(TokenType.PLUS_EQUAL);
                else                 addToken(TokenType.PLUS);
            }
            case '-' -> {
                if (match('-'))      addToken(TokenType.MINUS_MINUS);
                else if (match('=')) addToken(TokenType.MINUS_EQUAL);
                else                 addToken(TokenType.MINUS);
            }
            case '*' -> {
                if (match('*'))      addToken(TokenType.STAR_STAR);
                else if (match('=')) addToken(TokenType.STAR_EQUAL);
                else                 addToken(TokenType.STAR);
            }
            case '/' -> {
                if (match('/')) {
                    // Single-line comment — skip to end of line
                    while (!isAtEnd() && peek() != '\n') advance();
                } else if (match('*')) {
                    // Multi-line comment — skip to */
                    blockComment();
                } else if (match('=')) {
                    addToken(TokenType.SLASH_EQUAL);
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            case '%' -> {
                if (match('=')) addToken(TokenType.PERCENT_EQUAL);
                else            addToken(TokenType.PERCENT);
            }

            // ── Comparison / assignment / arrow ─────────────────────
            case '=' -> {
                if (match('=')) {
                    if (match('=')) addToken(TokenType.EQUAL_EQUAL_EQUAL);
                    else            addToken(TokenType.EQUAL_EQUAL);
                } else if (match('>')) {
                    addToken(TokenType.ARROW);
                } else {
                    addToken(TokenType.EQUAL);
                }
            }
            case '!' -> {
                if (match('=')) {
                    if (match('=')) addToken(TokenType.BANG_EQUAL_EQUAL);
                    else            addToken(TokenType.BANG_EQUAL);
                } else {
                    addToken(TokenType.BANG);
                }
            }
            case '<' -> {
                if (match('=')) addToken(TokenType.LESS_EQUAL);
                else            addToken(TokenType.LESS);
            }
            case '>' -> {
                if (match('=')) addToken(TokenType.GREATER_EQUAL);
                else            addToken(TokenType.GREATER);
            }

            // ── Logical operators ───────────────────────────────────
            case '&' -> {
                if (match('&')) addToken(TokenType.AND_AND);
                else            error("Unexpected character '&'. Did you mean '&&'?");
            }
            case '|' -> {
                if (match('|')) addToken(TokenType.OR_OR);
                else            error("Unexpected character '|'. Did you mean '||'?");
            }

            // ── Strings ────────────────────────────────────────────
            case '"'  -> string('"');
            case '\'' -> string('\'');
            case '`'  -> templateLiteral();

            // ── Whitespace ──────────────────────────────────────────
            case ' ', '\r', '\t' -> { /* skip */ }
            case '\n' -> {
                line++;
                column = 1;
            }

            // ── Numbers and identifiers ─────────────────────────────
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error("Unexpected character '" + c + "'");
                }
            }
        }
    }

    // ── String scanning ─────────────────────────────────────────────────

    /**
     * Scan a single- or double-quoted string literal.
     * Supports basic escape sequences: \n \t \\ \' \" \\
     */
    private void string(char quote) {
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && peek() != quote) {
            char ch = peek();
            if (ch == '\n') {
                line++;
                column = 0; // will be incremented by advance()
            }
            if (ch == '\\') {
                advance(); // skip backslash
                char escaped = advance();
                switch (escaped) {
                    case 'n'  -> sb.append('\n');
                    case 't'  -> sb.append('\t');
                    case '\\' -> sb.append('\\');
                    case '\'' -> sb.append('\'');
                    case '"'  -> sb.append('"');
                    default   -> { sb.append('\\'); sb.append(escaped); }
                }
            } else {
                sb.append(advance());
            }
        }

        if (isAtEnd()) {
            error("Unterminated string literal");
            return;
        }

        advance(); // consume closing quote

        addToken(TokenType.STRING, sb.toString());
    }

    /**
     * Scan a template literal (backtick string).
     * We emit the entire template (including ${...} expressions) as a
     * single TEMPLATE_LITERAL token whose literal value is the raw text
     * between backticks. The parser will handle interpolation parsing.
     */
    private void templateLiteral() {
        StringBuilder raw = new StringBuilder();
        int braceDepth = 0;

        while (!isAtEnd()) {
            char ch = peek();

            if (ch == '`' && braceDepth == 0) {
                break; // end of template
            }

            if (ch == '\\') {
                advance(); // skip backslash
                char escaped = advance();
                switch (escaped) {
                    case 'n'  -> raw.append('\n');
                    case 't'  -> raw.append('\t');
                    case '\\' -> raw.append('\\');
                    case '`'  -> raw.append('`');
                    case '$'  -> raw.append('$');
                    default   -> { raw.append('\\'); raw.append(escaped); }
                }
                continue;
            }

            if (ch == '$' && current + 1 < source.length() && source.charAt(current + 1) == '{') {
                raw.append(advance()); // $
                raw.append(advance()); // {
                braceDepth++;
                continue;
            }

            if (ch == '{') {
                braceDepth++;
            } else if (ch == '}') {
                if (braceDepth > 0) braceDepth--;
            }

            if (ch == '\n') {
                line++;
                column = 0;
            }
            raw.append(advance());
        }

        if (isAtEnd()) {
            error("Unterminated template literal");
            return;
        }

        advance(); // consume closing backtick

        addToken(TokenType.TEMPLATE_LITERAL, raw.toString());
    }

    // ── Number scanning ─────────────────────────────────────────────────

    private void number() {
        while (isDigit(peek())) advance();

        // Fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume the '.'
            while (isDigit(peek())) advance();
        }

        String text = source.substring(start, current);
        addToken(TokenType.NUMBER, Double.parseDouble(text));
    }

    // ── Identifier / keyword scanning ───────────────────────────────────

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);

        // For true/false/null/undefined, set the literal value
        Object literal = null;
        switch (type) {
            case TRUE      -> literal = Boolean.TRUE;
            case FALSE     -> literal = Boolean.FALSE;
            default -> { /* no literal */ }
        }

        addToken(type, literal);
    }

    // ── Comment scanning ────────────────────────────────────────────────

    private void blockComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // *
                advance(); // /
                return;
            }
            if (peek() == '\n') {
                line++;
                column = 0;
            }
            advance();
        }
        error("Unterminated block comment");
    }

    // ── Character helpers ───────────────────────────────────────────────

    private char advance() {
        char c = source.charAt(current);
        current++;
        column++;
        return c;
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        column++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_' || c == '$';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // ── Token emission ──────────────────────────────────────────────────

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line, startColumn));
    }

    // ── Error reporting ─────────────────────────────────────────────────

    private void error(String message) {
        throw new LexerError(message, line, startColumn);
    }

    /**
     * Exception thrown when the lexer encounters invalid input.
     */
    public static class LexerError extends RuntimeException {
        private final int line;
        private final int column;

        public LexerError(String message, int line, int column) {
            super(message);
            this.line = line;
            this.column = column;
        }

        public LexerError(String message) {
            super(message);
            this.line = -1;
            this.column = -1;
        }

        public int getLine() { return line; }
        public int getColumn() { return column; }
    }
}
