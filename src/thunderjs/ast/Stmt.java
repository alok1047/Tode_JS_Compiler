package thunderjs.ast;

import thunderjs.lexer.Token;

import java.util.List;

/**
 * Abstract base class for all statement AST nodes in ThunderJS.
 *
 * Uses the Visitor pattern: each concrete statement subclass implements
 * {@code accept(Visitor<R> visitor)} to dispatch to the correct visitor method.
 */
public abstract class Stmt {

    /**
     * Visitor interface for statement nodes.
     */
    public interface Visitor<R> {
        R visitExpressionStmt(ExpressionStmt stmt);
        R visitVarDeclarationStmt(VarDeclaration stmt);
        R visitBlockStmt(Block stmt);
        R visitIfStmt(If stmt);
        R visitForStmt(For stmt);
        R visitWhileStmt(While stmt);
        R visitDoWhileStmt(DoWhile stmt);
        R visitFunctionDeclStmt(FunctionDecl stmt);
        R visitReturnStmt(Return stmt);
        R visitSwitchStmt(Switch stmt);
        R visitBreakStmt(Break stmt);
        R visitContinueStmt(Continue stmt);
        R visitDestructuredVarDeclarationStmt(DestructuredVarDeclaration stmt);
        R visitForInStmt(ForIn stmt);
        R visitForOfStmt(ForOf stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    // ════════════════════════════════════════════════════════════════════
    //  STATEMENT NODE TYPES
    // ════════════════════════════════════════════════════════════════════

    /**
     * Expression statement: an expression followed by a semicolon.
     * e.g., console.log("hello");  or  x = 5;
     */
    public static class ExpressionStmt extends Stmt {
        public final Expr expression;

        public ExpressionStmt(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    /**
     * Variable declaration: let x = 5; or const y = "hello";
     */
    public static class VarDeclaration extends Stmt {
        public final Token keyword;        // let, const, or var
        public final Token name;           // variable name
        public final Expr initializer;     // nullable (let x;)

        public VarDeclaration(Token keyword, Token name, Expr initializer) {
            this.keyword = keyword;
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDeclarationStmt(this);
        }
    }

    /**
     * Block statement: { stmt1; stmt2; ... }
     */
    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    /**
     * If statement with optional else-if chains and else.
     * else-if is represented as a nested If in the elseBranch.
     */
    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch; // nullable

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    /**
     * For loop: for (init; condition; increment) body
     *
     * init can be a VarDeclaration or ExpressionStmt (or null).
     */
    public static class For extends Stmt {
        public final Stmt initializer;    // nullable
        public final Expr condition;       // nullable (infinite loop if null)
        public final Expr increment;       // nullable
        public final Stmt body;

        public For(Stmt initializer, Expr condition, Expr increment, Stmt body) {
            this.initializer = initializer;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }
    }

    /**
     * While loop: while (condition) body
     */
    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    /**
     * Do-while loop: do { body } while (condition);
     */
    public static class DoWhile extends Stmt {
        public final Stmt body;
        public final Expr condition;

        public DoWhile(Stmt body, Expr condition) {
            this.body = body;
            this.condition = condition;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitDoWhileStmt(this);
        }
    }

    /**
     * Function declaration: function name(params) { body }
     */
    public static class FunctionDecl extends Stmt {
        public final Token name;
        public final List<Expr.Parameter> params;
        public final List<Stmt> body;

        public FunctionDecl(Token name, List<Expr.Parameter> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionDeclStmt(this);
        }
    }

    /**
     * Return statement: return expr;
     */
    public static class Return extends Stmt {
        public final Token keyword;
        public final Expr value; // nullable (bare return)

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    /**
     * Switch statement: switch (expr) { case val: stmts; default: stmts; }
     */
    public static class Switch extends Stmt {
        public final Expr discriminant;
        public final List<Case> cases;

        public Switch(Expr discriminant, List<Case> cases) {
            this.discriminant = discriminant;
            this.cases = cases;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchStmt(this);
        }
    }

    /**
     * A single case or default clause within a switch statement.
     */
    public static class Case {
        public final Expr value;           // null for 'default'
        public final List<Stmt> body;

        public Case(Expr value, List<Stmt> body) {
            this.value = value;
            this.body = body;
        }
    }

    /**
     * Break statement: break;
     */
    public static class Break extends Stmt {
        public final Token keyword;

        public Break(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStmt(this);
        }
    }

    /**
     * Continue statement: continue;
     */
    public static class Continue extends Stmt {
        public final Token keyword;

        public Continue(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitContinueStmt(this);
        }
    }

    /**
     * Destructured variable declaration: const {name, age} = person
     */
    public static class DestructuredVarDeclaration extends Stmt {
        public final Token keyword;        // let, const, or var
        public final Expr pattern;         // ArrayLiteral or ObjectLiteral
        public final Expr initializer;     // nullable (e.g. inside for...in initializer)

        public DestructuredVarDeclaration(Token keyword, Expr pattern, Expr initializer) {
            this.keyword = keyword;
            this.pattern = pattern;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitDestructuredVarDeclarationStmt(this);
        }
    }

    /**
     * For...in loop statement: for (const key in obj) body
     */
    public static class ForIn extends Stmt {
        public final Stmt initializer;    // VarDeclaration, DestructuredVarDeclaration, or ExpressionStmt
        public final Expr object;
        public final Stmt body;

        public ForIn(Stmt initializer, Expr object, Stmt body) {
            this.initializer = initializer;
            this.object = object;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForInStmt(this);
        }
    }

    /**
     * For...of loop statement: for (const item of iterable) body
     */
    public static class ForOf extends Stmt {
        public final Stmt initializer;    // VarDeclaration, DestructuredVarDeclaration, or ExpressionStmt
        public final Expr iterable;
        public final Stmt body;

        public ForOf(Stmt initializer, Expr iterable, Stmt body) {
            this.initializer = initializer;
            this.iterable = iterable;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForOfStmt(this);
        }
    }
}
