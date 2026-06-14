package thunderjs.ast;

import thunderjs.lexer.Token;

import java.util.List;

/**
 * Abstract base class for all expression AST nodes in ThunderJS.
 *
 * Uses the Visitor pattern: each concrete expression subclass implements
 * {@code accept(Visitor<R> visitor)} to dispatch to the correct visitor method.
 *
 * All expression types are nested static classes inside this file for
 * cohesion and easy navigation.
 */
public abstract class Expr {

    /**
     * Visitor interface for expression nodes.
     * The Interpreter, ASTPrinter, etc. implement this interface.
     */
    public interface Visitor<R> {
        R visitLiteralExpr(Literal expr);
        R visitIdentifierExpr(Identifier expr);
        R visitBinaryExpr(Binary expr);
        R visitUnaryExpr(Unary expr);
        R visitLogicalExpr(Logical expr);
        R visitAssignExpr(Assign expr);
        R visitMemberAssignExpr(MemberAssign expr);
        R visitComputedAssignExpr(ComputedAssign expr);
        R visitCompoundAssignExpr(CompoundAssign expr);
        R visitUpdateExpr(Update expr);
        R visitCallExpr(Call expr);
        R visitMemberAccessExpr(MemberAccess expr);
        R visitComputedAccessExpr(ComputedAccess expr);
        R visitArrayLiteralExpr(ArrayLiteral expr);
        R visitObjectLiteralExpr(ObjectLiteral expr);
        R visitFunctionExpr(FunctionExpr expr);
        R visitArrowFunctionExpr(ArrowFunction expr);
        R visitTernaryExpr(Ternary expr);
        R visitSpreadExpr(Spread expr);
        R visitTemplateLiteralExpr(TemplateLiteral expr);
        R visitGroupingExpr(Grouping expr);
        R visitTypeofExpr(TypeofExpr expr);
        R visitNewExpr(New expr);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    // ════════════════════════════════════════════════════════════════════
    //  EXPRESSION NODE TYPES
    // ════════════════════════════════════════════════════════════════════

    /**
     * A literal value: number, string, boolean, null, undefined.
     */
    public static class Literal extends Expr {
        public final Object value;
        public final Token token;

        public Literal(Object value, Token token) {
            this.value = value;
            this.token = token;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    /**
     * A variable reference: x, foo, myVar.
     */
    public static class Identifier extends Expr {
        public final Token name;

        public Identifier(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIdentifierExpr(this);
        }
    }

    /**
     * A binary expression: left op right (e.g., x + y, a === b).
     */
    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    /**
     * A unary expression: !x, -x, +x, typeof x.
     */
    public static class Unary extends Expr {
        public final Token operator;
        public final Expr operand;

        public Unary(Token operator, Expr operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    /**
     * A logical expression: left && right, left || right.
     * Separated from Binary because of short-circuit evaluation.
     */
    public static class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    /**
     * Simple assignment: x = expr.
     */
    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    /**
     * Member assignment: obj.prop = expr.
     */
    public static class MemberAssign extends Expr {
        public final Expr object;
        public final Token name;
        public final Expr value;

        public MemberAssign(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitMemberAssignExpr(this);
        }
    }

    /**
     * Computed assignment: obj[index] = expr.
     */
    public static class ComputedAssign extends Expr {
        public final Expr object;
        public final Expr index;
        public final Expr value;
        public final Token bracket;

        public ComputedAssign(Expr object, Expr index, Expr value, Token bracket) {
            this.object = object;
            this.index = index;
            this.value = value;
            this.bracket = bracket;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitComputedAssignExpr(this);
        }
    }

    /**
     * Compound assignment: x += expr, x -= expr, x *= expr, x /= expr.
     */
    public static class CompoundAssign extends Expr {
        public final Expr target;
        public final Token operator;
        public final Expr value;

        public CompoundAssign(Expr target, Token operator, Expr value) {
            this.target = target;
            this.operator = operator;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCompoundAssignExpr(this);
        }
    }

    /**
     * Update expression: x++ or x-- (postfix only for now).
     */
    public static class Update extends Expr {
        public final Expr target;
        public final Token operator;
        public final boolean isPrefix;

        public Update(Expr target, Token operator, boolean isPrefix) {
            this.target = target;
            this.operator = operator;
            this.isPrefix = isPrefix;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUpdateExpr(this);
        }
    }

    /**
     * Function/method call: callee(arg1, arg2, ...).
     */
    public static class Call extends Expr {
        public final Expr callee;
        public final Token paren; // for error reporting (line number)
        public final List<Expr> arguments;

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    /**
     * Dot property access: object.property.
     */
    public static class MemberAccess extends Expr {
        public final Expr object;
        public final Token name;

        public MemberAccess(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitMemberAccessExpr(this);
        }
    }

    /**
     * Bracket property access: object[expr].
     */
    public static class ComputedAccess extends Expr {
        public final Expr object;
        public final Expr index;
        public final Token bracket; // for error reporting

        public ComputedAccess(Expr object, Expr index, Token bracket) {
            this.object = object;
            this.index = index;
            this.bracket = bracket;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitComputedAccessExpr(this);
        }
    }

    /**
     * Array literal: [1, 2, 3] or [...arr, 4].
     */
    public static class ArrayLiteral extends Expr {
        public final List<Expr> elements; // may contain Spread expressions
        public final Token bracket;

        public ArrayLiteral(List<Expr> elements, Token bracket) {
            this.elements = elements;
            this.bracket = bracket;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayLiteralExpr(this);
        }
    }

    /**
     * Object literal: { key: value, key2: value2 }.
     */
    public static class ObjectLiteral extends Expr {
        public final List<String> keys;
        public final List<Expr> values;
        public final Token brace;

        public ObjectLiteral(List<String> keys, List<Expr> values, Token brace) {
            this.keys = keys;
            this.values = values;
            this.brace = brace;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitObjectLiteralExpr(this);
        }
    }

    /**
     * Function expression: function(a, b) { return a + b; }
     */
    public static class FunctionExpr extends Expr {
        public final Token name; // nullable (anonymous)
        public final List<Parameter> params;
        public final List<Stmt> body;

        public FunctionExpr(Token name, List<Parameter> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionExpr(this);
        }
    }

    /**
     * Arrow function: (a, b) => a + b  or  (a, b) => { ... }
     */
    public static class ArrowFunction extends Expr {
        public final List<Parameter> params;
        public final List<Stmt> body;       // block body: { stmt; stmt; }
        public final Expr expression;       // concise body: expr  (null if block)

        public ArrowFunction(List<Parameter> params, List<Stmt> body, Expr expression) {
            this.params = params;
            this.body = body;
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrowFunctionExpr(this);
        }
    }

    /**
     * Ternary conditional: condition ? then : else.
     */
    public static class Ternary extends Expr {
        public final Expr condition;
        public final Expr thenBranch;
        public final Expr elseBranch;

        public Ternary(Expr condition, Expr thenBranch, Expr elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTernaryExpr(this);
        }
    }

    /**
     * Spread expression: ...arr (inside array literals or function calls).
     */
    public static class Spread extends Expr {
        public final Expr expression;

        public Spread(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpreadExpr(this);
        }
    }

    /**
     * Template literal: `Hello ${name}, you are ${age} years old`.
     * Stored as alternating parts (strings) and expressions.
     */
    public static class TemplateLiteral extends Expr {
        public final List<String> parts;    // string segments
        public final List<Expr> expressions; // interpolated expressions
        public final Token token;

        public TemplateLiteral(List<String> parts, List<Expr> expressions, Token token) {
            this.parts = parts;
            this.expressions = expressions;
            this.token = token;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTemplateLiteralExpr(this);
        }
    }

    /**
     * Parenthesized expression: (expr).
     */
    public static class Grouping extends Expr {
        public final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    /**
     * typeof operator: typeof x.
     */
    public static class TypeofExpr extends Expr {
        public final Expr operand;
        public final Token keyword;

        public TypeofExpr(Expr operand, Token keyword) {
            this.operand = operand;
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTypeofExpr(this);
        }
    }

    /**
     * A constructor call: new constructor(args).
     */
    public static class New extends Expr {
        public final Token keyword;
        public final Expr constructor;
        public final List<Expr> arguments; // Nullable if no parens

        public New(Token keyword, Expr constructor, List<Expr> arguments) {
            this.keyword = keyword;
            this.constructor = constructor;
            this.arguments = arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitNewExpr(this);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  HELPER TYPES
    // ════════════════════════════════════════════════════════════════════

    /**
     * Represents a function parameter — may be a rest parameter (...args).
     */
    public static class Parameter {
        public final Token name;
        public final boolean isRest;
        public final Expr defaultValue; // nullable

        public Parameter(Token name, boolean isRest, Expr defaultValue) {
            this.name = name;
            this.isRest = isRest;
            this.defaultValue = defaultValue;
        }

        public Parameter(Token name, boolean isRest) {
            this(name, isRest, null);
        }
    }
}
