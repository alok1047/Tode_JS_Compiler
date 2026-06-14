package thunderjs.parser;

import thunderjs.ast.Expr;
import thunderjs.ast.Stmt;
import thunderjs.lexer.Token;
import thunderjs.lexer.TokenType;
import thunderjs.runtime.JSNull;
import thunderjs.runtime.JSUndefined;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser for ThunderJS.
 *
 * Converts a list of {@link Token}s into a list of {@link Stmt} AST nodes.
 *
 * Precedence table (low → high):
 *   1. Assignment       =, +=, -=, *=, /=      (right)
 *   2. Ternary          ? :                     (right)
 *   3. Logical OR       ||                      (left)
 *   4. Logical AND      &&                      (left)
 *   5. Equality         ==, ===, !=, !==        (left)
 *   6. Comparison       <, >, <=, >=            (left)
 *   7. Addition         +, -                    (left)
 *   8. Multiplication   *, /, %                 (left)
 *   9. Exponentiation   **                      (right)
 *  10. Unary            !, -, +, typeof, ++, -- (right, prefix)
 *  11. Postfix          ++, --                  (left)
 *  12. Call / Member     (), ., []              (left)
 *  13. Primary          literals, identifiers, grouping, arrays, objects, functions
 */
public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // ════════════════════════════════════════════════════════════════════
    //  PUBLIC API
    // ════════════════════════════════════════════════════════════════════

    /**
     * Parse the entire token stream into a list of statements.
     */
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            Stmt stmt = declaration();
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        return statements;
    }

    // ════════════════════════════════════════════════════════════════════
    //  STATEMENT PARSING
    // ════════════════════════════════════════════════════════════════════

    /**
     * declaration → varDeclaration | functionDeclaration | statement
     */
    private Stmt declaration() {
        if (check(TokenType.LET) || check(TokenType.CONST) || check(TokenType.VAR)) {
            return varDeclaration();
        }
        if (check(TokenType.FUNCTION) && checkNext(TokenType.IDENTIFIER)) {
            return functionDeclaration();
        }
        return statement();
    }

    /**
     * varDeclaration → ("let" | "const" | "var") IDENTIFIER ("=" expression)? ";"
     */
    private Stmt varDeclaration() {
        Token keyword = advance(); // let, const, or var

        Token name = consume(TokenType.IDENTIFIER, "Expected variable name after '" + keyword.getLexeme() + "'");

        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consumeSemicolon();
        return new Stmt.VarDeclaration(keyword, name, initializer);
    }

    /**
     * functionDeclaration → "function" IDENTIFIER "(" params ")" "{" body "}"
     */
    private Stmt functionDeclaration() {
        advance(); // consume 'function'
        Token name = consume(TokenType.IDENTIFIER, "Expected function name");
        consume(TokenType.LEFT_PAREN, "Expected '(' after function name");

        List<Expr.Parameter> params = parseParameterList();

        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters");
        consume(TokenType.LEFT_BRACE, "Expected '{' before function body");

        List<Stmt> body = blockBody();

        return new Stmt.FunctionDecl(name, params, body);
    }

    /**
     * statement → ifStmt | forStmt | whileStmt | doWhileStmt | switchStmt
     *           | returnStmt | breakStmt | continueStmt | block | exprStmt
     */
    private Stmt statement() {
        if (match(TokenType.IF))       return ifStatement();
        if (match(TokenType.FOR))      return forStatement();
        if (match(TokenType.WHILE))    return whileStatement();
        if (match(TokenType.DO))       return doWhileStatement();
        if (match(TokenType.SWITCH))   return switchStatement();
        if (match(TokenType.RETURN))   return returnStatement();
        if (match(TokenType.BREAK)) {
            Token keyword = previous();
            consumeSemicolon();
            return new Stmt.Break(keyword);
        }
        if (match(TokenType.CONTINUE)) {
            Token keyword = previous();
            consumeSemicolon();
            return new Stmt.Continue(keyword);
        }
        if (check(TokenType.LEFT_BRACE)) return block();
        return expressionStatement();
    }

    /**
     * block → "{" declaration* "}"
     */
    private Stmt block() {
        consume(TokenType.LEFT_BRACE, "Expected '{'");
        List<Stmt> statements = blockBody();
        return new Stmt.Block(statements);
    }

    /**
     * Parse the body of a block (after '{' has been consumed).
     * Consumes the closing '}'.
     */
    private List<Stmt> blockBody() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            Stmt stmt = declaration();
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after block");
        return statements;
    }

    /**
     * ifStmt → "if" "(" expression ")" statement ("else" statement)?
     */
    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    /**
     * forStmt → "for" "(" (varDecl | exprStmt | ";") expression? ";" expression? ")" statement
     */
    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'");

        // Initializer
        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (check(TokenType.LET) || check(TokenType.CONST) || check(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        // Condition
        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after for-loop condition");

        // Increment
        Expr increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after for clauses");

        Stmt body = statement();

        return new Stmt.For(initializer, condition, increment, body);
    }

    /**
     * whileStmt → "while" "(" expression ")" statement
     */
    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after while condition");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    /**
     * doWhileStmt → "do" statement "while" "(" expression ")" ";"
     */
    private Stmt doWhileStatement() {
        Stmt body = statement();
        consume(TokenType.WHILE, "Expected 'while' after do body");
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after do-while condition");
        consumeSemicolon();
        return new Stmt.DoWhile(body, condition);
    }

    /**
     * switchStmt → "switch" "(" expression ")" "{" caseClause* "}"
     */
    private Stmt switchStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'switch'");
        Expr discriminant = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after switch expression");
        consume(TokenType.LEFT_BRACE, "Expected '{' before switch body");

        List<Stmt.Case> cases = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.CASE)) {
                Expr value = expression();
                consume(TokenType.COLON, "Expected ':' after case value");
                List<Stmt> body = caseBody();
                cases.add(new Stmt.Case(value, body));
            } else if (match(TokenType.DEFAULT)) {
                consume(TokenType.COLON, "Expected ':' after 'default'");
                List<Stmt> body = caseBody();
                cases.add(new Stmt.Case(null, body));
            } else {
                throw error(peek(), "Expected 'case' or 'default' in switch body");
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after switch body");
        return new Stmt.Switch(discriminant, cases);
    }

    /**
     * Parse statements within a case clause (until next case/default/}).
     */
    private List<Stmt> caseBody() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.CASE) && !check(TokenType.DEFAULT) &&
               !check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            Stmt stmt = declaration();
            if (stmt != null) {
                stmts.add(stmt);
            }
        }
        return stmts;
    }

    /**
     * returnStmt → "return" expression? ";"
     */
    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON) && !check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            value = expression();
        }
        consumeSemicolon();
        return new Stmt.Return(keyword, value);
    }

    /**
     * exprStmt → expression ";"
     */
    private Stmt expressionStatement() {
        Expr expr = expression();
        consumeSemicolon();
        return new Stmt.ExpressionStmt(expr);
    }

    // ════════════════════════════════════════════════════════════════════
    //  EXPRESSION PARSING (by precedence, low → high)
    // ════════════════════════════════════════════════════════════════════

    /**
     * expression → assignment
     */
    private Expr expression() {
        return assignment();
    }

    /**
     * assignment → (IDENTIFIER ("=" | "+=" | "-=" | "*=" | "/=" | "%=") assignment)
     *            | ternary
     *
     * Also handles member/computed assignment: obj.x = val, arr[0] = val.
     */
    private Expr assignment() {
        Expr expr = ternary();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment(); // right-associative

            if (expr instanceof Expr.Identifier id) {
                return new Expr.Assign(id.name, value);
            } else if (expr instanceof Expr.MemberAccess mem) {
                return new Expr.MemberAssign(mem.object, mem.name, value);
            } else if (expr instanceof Expr.ComputedAccess comp) {
                return new Expr.ComputedAssign(comp.object, comp.index, value, comp.bracket);
            }
            throw error(equals, "Invalid assignment target");
        }

        // Compound assignment: +=, -=, *=, /=, %=
        if (match(TokenType.PLUS_EQUAL, TokenType.MINUS_EQUAL,
                  TokenType.STAR_EQUAL, TokenType.SLASH_EQUAL, TokenType.PERCENT_EQUAL)) {
            Token op = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Identifier id) {
                return new Expr.CompoundAssign(id.name, op, value);
            }
            throw error(op, "Invalid compound assignment target");
        }

        return expr;
    }

    /**
     * ternary → logicalOr ("?" expression ":" ternary)?
     */
    private Expr ternary() {
        Expr expr = logicalOr();

        if (match(TokenType.QUESTION)) {
            Expr thenBranch = expression();
            consume(TokenType.COLON, "Expected ':' in ternary expression");
            Expr elseBranch = ternary();
            return new Expr.Ternary(expr, thenBranch, elseBranch);
        }

        return expr;
    }

    /**
     * logicalOr → logicalAnd ("||" logicalAnd)*
     */
    private Expr logicalOr() {
        Expr expr = logicalAnd();

        while (match(TokenType.OR_OR)) {
            Token op = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, op, right);
        }

        return expr;
    }

    /**
     * logicalAnd → equality ("&&" equality)*
     */
    private Expr logicalAnd() {
        Expr expr = equality();

        while (match(TokenType.AND_AND)) {
            Token op = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, op, right);
        }

        return expr;
    }

    /**
     * equality → comparison (("==" | "===" | "!=" | "!==") comparison)*
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.EQUAL_EQUAL, TokenType.EQUAL_EQUAL_EQUAL,
                     TokenType.BANG_EQUAL, TokenType.BANG_EQUAL_EQUAL)) {
            Token op = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    /**
     * comparison → addition (("<" | ">" | "<=" | ">=") addition)*
     */
    private Expr comparison() {
        Expr expr = addition();

        while (match(TokenType.LESS, TokenType.GREATER,
                     TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {
            Token op = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    /**
     * addition → multiplication (("+" | "-") multiplication)*
     */
    private Expr addition() {
        Expr expr = multiplication();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    /**
     * multiplication → exponentiation (("*" | "/" | "%") exponentiation)*
     */
    private Expr multiplication() {
        Expr expr = exponentiation();

        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token op = previous();
            Expr right = exponentiation();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    /**
     * exponentiation → unary ("**" exponentiation)?  (right-associative)
     */
    private Expr exponentiation() {
        Expr expr = unary();

        if (match(TokenType.STAR_STAR)) {
            Token op = previous();
            Expr right = exponentiation(); // right-associative
            return new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    /**
     * unary → ("!" | "-" | "+" | "typeof") unary
     *       | ("++" | "--") IDENTIFIER   (prefix)
     *       | postfix
     */
    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token op = previous();
            Expr operand = unary();
            return new Expr.Unary(op, operand);
        }

        if (match(TokenType.TYPEOF)) {
            Token keyword = previous();
            Expr operand = unary();
            return new Expr.TypeofExpr(operand, keyword);
        }

        // Handle unary +
        if (match(TokenType.PLUS)) {
            // Unary plus — evaluate operand and convert to number
            Token op = previous();
            Expr operand = unary();
            return new Expr.Unary(op, operand);
        }

        // Prefix ++ / --
        if (match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {
            Token op = previous();
            if (check(TokenType.IDENTIFIER)) {
                Token name = advance();
                return new Expr.Update(name, op, true);
            }
            throw error(op, "Invalid prefix update operand");
        }

        return postfix();
    }

    /**
     * postfix → call ("++" | "--")?
     */
    private Expr postfix() {
        Expr expr = call();

        if (match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {
            Token op = previous();
            if (expr instanceof Expr.Identifier id) {
                return new Expr.Update(id.name, op, false);
            }
            throw error(op, "Invalid postfix update operand");
        }

        return expr;
    }

    /**
     * call → primary ( "(" args ")" | "." IDENTIFIER | "[" expression "]" )*
     */
    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expected property name after '.'");
                expr = new Expr.MemberAccess(expr, name);
            } else if (match(TokenType.LEFT_BRACKET)) {
                Token bracket = previous();
                Expr index = expression();
                consume(TokenType.RIGHT_BRACKET, "Expected ']' after index");
                expr = new Expr.ComputedAccess(expr, index, bracket);
            } else {
                break;
            }
        }

        return expr;
    }

    /**
     * Finish parsing a function call after '(' has been consumed.
     */
    private Expr finishCall(Expr callee) {
        List<Expr> args = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                // Support spread in arguments
                if (match(TokenType.DOT_DOT_DOT)) {
                    Expr spread = assignment();
                    args.add(new Expr.Spread(spread));
                } else {
                    args.add(assignment());
                }
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments");
        return new Expr.Call(callee, paren, args);
    }

    // ════════════════════════════════════════════════════════════════════
    //  PRIMARY EXPRESSIONS
    // ════════════════════════════════════════════════════════════════════

    /**
     * primary → NUMBER | STRING | TRUE | FALSE | NULL | UNDEFINED
     *         | IDENTIFIER
     *         | "(" expression ")"      ← grouping or arrow function
     *         | "[" elements "]"         ← array literal
     *         | "{" entries "}"          ← object literal
     *         | TEMPLATE_LITERAL
     *         | "function" ...           ← function expression
     */
    private Expr member() {
        Expr expr = primary();

        while (true) {
            if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expected property name after '.'");
                expr = new Expr.MemberAccess(expr, name);
            } else if (match(TokenType.LEFT_BRACKET)) {
                Token bracket = previous();
                Expr index = expression();
                consume(TokenType.RIGHT_BRACKET, "Expected ']' after index");
                expr = new Expr.ComputedAccess(expr, index, bracket);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr primary() {
        if (match(TokenType.NEW)) {
            Token keyword = previous();
            Expr constructor = member();
            List<Expr> arguments = null;
            if (match(TokenType.LEFT_PAREN)) {
                arguments = new ArrayList<>();
                if (!check(TokenType.RIGHT_PAREN)) {
                    do {
                        if (match(TokenType.DOT_DOT_DOT)) {
                            Expr spread = assignment();
                            arguments.add(new Expr.Spread(spread));
                        } else {
                            arguments.add(assignment());
                        }
                    } while (match(TokenType.COMMA));
                }
                consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments");
            }
            return new Expr.New(keyword, constructor, arguments);
        }

        // Numeric literal
        if (match(TokenType.NUMBER)) {
            return new Expr.Literal(previous().getLiteral(), previous());
        }

        // String literal
        if (match(TokenType.STRING)) {
            return new Expr.Literal(previous().getLiteral(), previous());
        }

        // Boolean / null / undefined literals
        if (match(TokenType.TRUE))      return new Expr.Literal(true, previous());
        if (match(TokenType.FALSE))     return new Expr.Literal(false, previous());
        if (match(TokenType.NULL))      return new Expr.Literal(JSNull.INSTANCE, previous());
        if (match(TokenType.UNDEFINED)) return new Expr.Literal(JSUndefined.INSTANCE, previous());

        // Template literal
        if (match(TokenType.TEMPLATE_LITERAL)) {
            return parseTemplateLiteral(previous());
        }

        // Identifier (or possible arrow function: ident => expr)
        if (match(TokenType.IDENTIFIER)) {
            Token name = previous();

            // Single-param arrow function without parens: x => x * 2
            if (check(TokenType.ARROW)) {
                advance(); // consume =>
                return parseSingleParamArrow(name);
            }

            return new Expr.Identifier(name);
        }

        // Grouping or arrow function
        if (match(TokenType.LEFT_PAREN)) {
            return parseParenExpr();
        }

        // Array literal
        if (match(TokenType.LEFT_BRACKET)) {
            return parseArrayLiteral();
        }

        // Object literal
        if (match(TokenType.LEFT_BRACE)) {
            return parseObjectLiteral();
        }

        // Function expression
        if (match(TokenType.FUNCTION)) {
            return parseFunctionExpression();
        }

        throw error(peek(), "Unexpected token: " + peek().getLexeme());
    }

    // ── Parenthesized expression or arrow function ──────────────────────

    /**
     * Disambiguate between grouping (expr) and arrow function (a, b) => ...
     *
     * Strategy: if we see ')' followed by '=>', it's an arrow function.
     * Otherwise it's a grouping expression.
     */
    private Expr parseParenExpr() {
        // Empty parens → arrow function: () => ...
        if (check(TokenType.RIGHT_PAREN)) {
            advance(); // consume )
            consume(TokenType.ARROW, "Expected '=>' after empty parameter list");
            return parseArrowBody(new ArrayList<>());
        }

        // Try to detect arrow function by looking ahead
        // Save position for backtracking
        int savedPos = current;

        // Attempt to parse as parameter list
        if (couldBeArrowParams()) {
            // Reset and parse properly
            current = savedPos;
            List<Expr.Parameter> params = parseParameterList();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters");
            consume(TokenType.ARROW, "Expected '=>'");
            return parseArrowBody(params);
        }

        // It's a regular grouping expression
        current = savedPos;
        Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after expression");
        return new Expr.Grouping(expr);
    }

    /**
     * Lookahead to check if current position starts arrow function params.
     * Returns true if we find ) => pattern at correct depth.
     */
    private boolean couldBeArrowParams() {
        int depth = 0;
        int pos = current;

        while (pos < tokens.size()) {
            TokenType t = tokens.get(pos).getType();
            if (t == TokenType.LEFT_PAREN) depth++;
            if (t == TokenType.RIGHT_PAREN) {
                if (depth == 0) {
                    // Check if next is =>
                    return pos + 1 < tokens.size() &&
                           tokens.get(pos + 1).getType() == TokenType.ARROW;
                }
                depth--;
            }
            // If we encounter something that can't be in a param list, bail
            if (t == TokenType.SEMICOLON || t == TokenType.LEFT_BRACE ||
                t == TokenType.EOF) {
                return false;
            }
            pos++;
        }
        return false;
    }

    /**
     * Parse arrow function body (after => consumed).
     */
    private Expr parseArrowBody(List<Expr.Parameter> params) {
        if (check(TokenType.LEFT_BRACE)) {
            // Block body: (a, b) => { ... }
            advance(); // consume {
            List<Stmt> body = blockBody();
            return new Expr.ArrowFunction(params, body, null);
        } else {
            // Concise body: (a, b) => expr
            Expr body = assignment(); // assignment precedence for concise body
            return new Expr.ArrowFunction(params, null, body);
        }
    }

    /**
     * Parse a single-parameter arrow function: x => expr
     */
    private Expr parseSingleParamArrow(Token paramName) {
        List<Expr.Parameter> params = new ArrayList<>();
        params.add(new Expr.Parameter(paramName, false));
        return parseArrowBody(params);
    }

    // ── Array literal ───────────────────────────────────────────────────

    private Expr parseArrayLiteral() {
        Token bracket = previous();
        List<Expr> elements = new ArrayList<>();

        if (!check(TokenType.RIGHT_BRACKET)) {
            do {
                if (check(TokenType.RIGHT_BRACKET)) break; // trailing comma
                if (match(TokenType.DOT_DOT_DOT)) {
                    Expr spread = assignment();
                    elements.add(new Expr.Spread(spread));
                } else {
                    elements.add(assignment());
                }
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_BRACKET, "Expected ']' after array elements");
        return new Expr.ArrayLiteral(elements, bracket);
    }

    // ── Object literal ──────────────────────────────────────────────────

    private Expr parseObjectLiteral() {
        Token brace = previous();
        List<String> keys = new ArrayList<>();
        List<Expr> values = new ArrayList<>();

        if (!check(TokenType.RIGHT_BRACE)) {
            do {
                if (check(TokenType.RIGHT_BRACE)) break; // trailing comma

                // Key can be identifier or string
                String key;
                if (match(TokenType.IDENTIFIER)) {
                    key = previous().getLexeme();
                } else if (match(TokenType.STRING)) {
                    key = (String) previous().getLiteral();
                } else if (match(TokenType.NUMBER)) {
                    key = previous().getLexeme();
                } else {
                    throw error(peek(), "Expected property name in object literal");
                }

                consume(TokenType.COLON, "Expected ':' after property name");
                Expr value = assignment();

                keys.add(key);
                values.add(value);
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after object literal");
        return new Expr.ObjectLiteral(keys, values, brace);
    }

    // ── Function expression ─────────────────────────────────────────────

    private Expr parseFunctionExpression() {
        Token name = null;
        if (check(TokenType.IDENTIFIER)) {
            name = advance();
        }

        consume(TokenType.LEFT_PAREN, "Expected '(' after function");
        List<Expr.Parameter> params = parseParameterList();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters");
        consume(TokenType.LEFT_BRACE, "Expected '{' before function body");
        List<Stmt> body = blockBody();

        return new Expr.FunctionExpr(name, params, body);
    }

    // ── Template literal parsing ────────────────────────────────────────

    /**
     * Parse a template literal token into parts and expressions.
     *
     * The lexer emits the raw content as a single TEMPLATE_LITERAL token.
     * We need to split it into string parts and ${expr} interpolations.
     */
    private Expr parseTemplateLiteral(Token token) {
        String raw = (String) token.getLiteral();
        List<String> parts = new ArrayList<>();
        List<Expr> expressions = new ArrayList<>();

        StringBuilder currentPart = new StringBuilder();
        int i = 0;

        while (i < raw.length()) {
            if (i + 1 < raw.length() && raw.charAt(i) == '$' && raw.charAt(i + 1) == '{') {
                // Found interpolation
                parts.add(currentPart.toString());
                currentPart = new StringBuilder();
                i += 2; // skip ${

                // Extract expression text
                int braceDepth = 1;
                StringBuilder exprText = new StringBuilder();
                while (i < raw.length() && braceDepth > 0) {
                    char c = raw.charAt(i);
                    if (c == '{') braceDepth++;
                    else if (c == '}') {
                        braceDepth--;
                        if (braceDepth == 0) { i++; break; }
                    }
                    exprText.append(c);
                    i++;
                }

                // Re-lex and re-parse the expression
                thunderjs.lexer.Lexer exprLexer = new thunderjs.lexer.Lexer(exprText.toString());
                List<Token> exprTokens = exprLexer.tokenize();
                Parser exprParser = new Parser(exprTokens);
                expressions.add(exprParser.expression());
            } else {
                currentPart.append(raw.charAt(i));
                i++;
            }
        }

        parts.add(currentPart.toString());

        return new Expr.TemplateLiteral(parts, expressions, token);
    }

    // ── Parameter list parsing ──────────────────────────────────────────

    /**
     * Parse a comma-separated parameter list (without parens).
     * Supports rest parameters: ...args
     */
    private List<Expr.Parameter> parseParameterList() {
        List<Expr.Parameter> params = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                boolean isRest = match(TokenType.DOT_DOT_DOT);
                Token paramName = consume(TokenType.IDENTIFIER, "Expected parameter name");

                Expr defaultValue = null;
                if (match(TokenType.EQUAL)) {
                    defaultValue = assignment();
                }

                params.add(new Expr.Parameter(paramName, isRest, defaultValue));
            } while (match(TokenType.COMMA));
        }

        return params;
    }

    // ════════════════════════════════════════════════════════════════════
    //  TOKEN NAVIGATION HELPERS
    // ════════════════════════════════════════════════════════════════════

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    /**
     * Consume a semicolon if present — lenient (JS allows ASI in many contexts).
     */
    private void consumeSemicolon() {
        match(TokenType.SEMICOLON);
    }

    // ════════════════════════════════════════════════════════════════════
    //  ERROR HANDLING
    // ════════════════════════════════════════════════════════════════════

    private ParseError error(Token token, String message) {
        String location = token.getType() == TokenType.EOF ? "end of input" :
                "'" + token.getLexeme() + "'";
        return new ParseError(message + " at " + location, token.getLine(), token.getColumn(), token.getLexeme());
    }

    public static class ParseError extends RuntimeException {
        private final int line;
        private final int column;
        private final String lexeme;

        public ParseError(String message, int line, int column, String lexeme) {
            super(message);
            this.line = line;
            this.column = column;
            this.lexeme = lexeme;
        }

        public ParseError(String message) {
            super(message);
            this.line = -1;
            this.column = -1;
            this.lexeme = null;
        }

        public int getLine() { return line; }
        public int getColumn() { return column; }
        public String getLexeme() { return lexeme; }
    }
}
