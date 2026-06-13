package thunderjs.lexer;

/**
 * Enumeration of every token type recognized by the ThunderJS lexer.
 *
 * Organized by category for readability:
 *   - Literals and identifiers
 *   - Keywords
 *   - Operators (arithmetic, comparison, logical, assignment)
 *   - Delimiters and punctuation
 *   - Special / internal tokens
 */
public enum TokenType {

    // ── Literals & Identifiers ──────────────────────────────────────────
    NUMBER,             // 42, 3.14
    STRING,             // "hello", 'world'
    TEMPLATE_LITERAL,   // `hello ${name}`
    IDENTIFIER,         // x, foo, myVar
    TRUE,               // true
    FALSE,              // false
    NULL,               // null
    UNDEFINED,          // undefined

    // ── Keywords ────────────────────────────────────────────────────────
    LET,
    CONST,
    VAR,
    IF,
    ELSE,
    FOR,
    WHILE,
    DO,
    SWITCH,
    CASE,
    DEFAULT,
    BREAK,
    CONTINUE,
    FUNCTION,
    RETURN,
    TYPEOF,
    NEW,

    // ── Arithmetic Operators ────────────────────────────────────────────
    PLUS,               // +
    MINUS,              // -
    STAR,               // *
    SLASH,              // /
    PERCENT,            // %
    STAR_STAR,          // **

    // ── Comparison Operators ────────────────────────────────────────────
    EQUAL_EQUAL,        // ==
    EQUAL_EQUAL_EQUAL,  // ===
    BANG_EQUAL,         // !=
    BANG_EQUAL_EQUAL,   // !==
    LESS,               // <
    LESS_EQUAL,         // <=
    GREATER,            // >
    GREATER_EQUAL,      // >=

    // ── Logical Operators ───────────────────────────────────────────────
    AND_AND,            // &&
    OR_OR,              // ||
    BANG,               // !

    // ── Assignment Operators ────────────────────────────────────────────
    EQUAL,              // =
    PLUS_EQUAL,         // +=
    MINUS_EQUAL,        // -=
    STAR_EQUAL,         // *=
    SLASH_EQUAL,        // /=
    PERCENT_EQUAL,      // %=

    // ── Update Operators ────────────────────────────────────────────────
    PLUS_PLUS,          // ++
    MINUS_MINUS,        // --

    // ── Delimiters & Punctuation ────────────────────────────────────────
    LEFT_PAREN,         // (
    RIGHT_PAREN,        // )
    LEFT_BRACE,         // {
    RIGHT_BRACE,        // }
    LEFT_BRACKET,       // [
    RIGHT_BRACKET,      // ]
    SEMICOLON,          // ;
    COMMA,              // ,
    DOT,                // .
    COLON,              // :
    QUESTION,           // ?

    // ── Special Operators ───────────────────────────────────────────────
    ARROW,              // =>
    DOT_DOT_DOT,        // ...

    // ── End of File ─────────────────────────────────────────────────────
    EOF
}
