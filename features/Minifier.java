package features;

import thunderjs.ast.Expr;
import thunderjs.ast.Stmt;

import java.util.List;

/**
 * AST-based JavaScript minifier.
 *
 * Walks the parsed AST and emits the most compact possible JavaScript
 * source code — no whitespace, no comments, no newlines except where
 * syntactically required. Useful for reducing code size.
 */
public class Minifier implements Expr.Visitor<String>, Stmt.Visitor<String> {

    /**
     * Minify a list of statements into a compact single-line (or near) source string.
     */
    public String minify(List<Stmt> statements) {
        StringBuilder sb = new StringBuilder();
        for (Stmt stmt : statements) {
            sb.append(stmt.accept(this));
        }
        return sb.toString();
    }

    private String formatParams(List<Expr.Parameter> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            Expr.Parameter p = params.get(i);
            if (p.isRest) sb.append("...");
            sb.append(p.name.getLexeme());
            if (p.defaultValue != null) {
                sb.append("=").append(p.defaultValue.accept(this));
            }
            if (i < params.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════════════
    //  STATEMENT VISITORS
    // ════════════════════════════════════════════════════════════════════

    @Override
    public String visitExpressionStmt(Stmt.ExpressionStmt stmt) {
        return stmt.expression.accept(this) + ";";
    }

    @Override
    public String visitVarDeclarationStmt(Stmt.VarDeclaration stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(stmt.keyword.getLexeme()).append(" ").append(stmt.name.getLexeme());
        if (stmt.initializer != null) {
            sb.append("=").append(stmt.initializer.accept(this));
        }
        sb.append(";");
        return sb.toString();
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Stmt s : stmt.statements) {
            sb.append(s.accept(this));
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("if(").append(stmt.condition.accept(this)).append(")");
        sb.append(wrapInBlock(stmt.thenBranch));
        if (stmt.elseBranch != null) {
            sb.append("else ");
            if (stmt.elseBranch instanceof Stmt.If) {
                sb.append(stmt.elseBranch.accept(this));
            } else {
                sb.append(wrapInBlock(stmt.elseBranch));
            }
        }
        return sb.toString();
    }

    @Override
    public String visitForStmt(Stmt.For stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("for(");
        if (stmt.initializer != null) {
            String init = stmt.initializer.accept(this);
            // Remove trailing semicolon from var declaration
            if (init.endsWith(";")) init = init.substring(0, init.length() - 1);
            sb.append(init);
        }
        sb.append(";");
        if (stmt.condition != null) sb.append(stmt.condition.accept(this));
        sb.append(";");
        if (stmt.increment != null) sb.append(stmt.increment.accept(this));
        sb.append(")");
        sb.append(wrapInBlock(stmt.body));
        return sb.toString();
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return "while(" + stmt.condition.accept(this) + ")" + wrapInBlock(stmt.body);
    }

    @Override
    public String visitDoWhileStmt(Stmt.DoWhile stmt) {
        return "do" + wrapInBlock(stmt.body) + "while(" + stmt.condition.accept(this) + ");";
    }

    @Override
    public String visitFunctionDeclStmt(Stmt.FunctionDecl stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("function ").append(stmt.name.getLexeme());
        sb.append("(").append(formatParams(stmt.params)).append("){");
        for (Stmt s : stmt.body) sb.append(s.accept(this));
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value != null) {
            return "return " + stmt.value.accept(this) + ";";
        }
        return "return;";
    }

    @Override
    public String visitSwitchStmt(Stmt.Switch stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("switch(").append(stmt.discriminant.accept(this)).append("){");
        for (Stmt.Case c : stmt.cases) {
            if (c.value != null) {
                sb.append("case ").append(c.value.accept(this)).append(":");
            } else {
                sb.append("default:");
            }
            for (Stmt s : c.body) sb.append(s.accept(this));
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return "break;";
    }

    @Override
    public String visitContinueStmt(Stmt.Continue stmt) {
        return "continue;";
    }

    // ════════════════════════════════════════════════════════════════════
    //  EXPRESSION VISITORS
    // ════════════════════════════════════════════════════════════════════

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "null";
        if (expr.value instanceof String s) {
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                          .replace("\n", "\\n").replace("\r", "\\r") + "\"";
        }
        if (expr.value instanceof Double d) {
            double dv = d;
            if (dv == (long) dv) return String.format("%d", (long) dv);
            return Double.toString(d);
        }
        return expr.value.toString();
    }

    @Override
    public String visitIdentifierExpr(Expr.Identifier expr) {
        return expr.name.getLexeme();
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return expr.left.accept(this) + expr.operator.getLexeme() + expr.right.accept(this);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return expr.operator.getLexeme() + expr.operand.accept(this);
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return expr.left.accept(this) + expr.operator.getLexeme() + expr.right.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return expr.name.getLexeme() + "=" + expr.value.accept(this);
    }

    @Override
    public String visitMemberAssignExpr(Expr.MemberAssign expr) {
        return expr.object.accept(this) + "." + expr.name.getLexeme() + "=" + expr.value.accept(this);
    }

    @Override
    public String visitComputedAssignExpr(Expr.ComputedAssign expr) {
        return expr.object.accept(this) + "[" + expr.index.accept(this) + "]=" + expr.value.accept(this);
    }

    @Override
    public String visitCompoundAssignExpr(Expr.CompoundAssign expr) {
        return expr.name.getLexeme() + expr.operator.getLexeme() + expr.value.accept(this);
    }

    @Override
    public String visitUpdateExpr(Expr.Update expr) {
        if (expr.isPrefix) return expr.operator.getLexeme() + expr.target.accept(this);
        return expr.target.accept(this) + expr.operator.getLexeme();
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        StringBuilder sb = new StringBuilder();
        sb.append(expr.callee.accept(this)).append("(");
        for (int i = 0; i < expr.arguments.size(); i++) {
            sb.append(expr.arguments.get(i).accept(this));
            if (i < expr.arguments.size() - 1) sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitMemberAccessExpr(Expr.MemberAccess expr) {
        return expr.object.accept(this) + "." + expr.name.getLexeme();
    }

    @Override
    public String visitComputedAccessExpr(Expr.ComputedAccess expr) {
        return expr.object.accept(this) + "[" + expr.index.accept(this) + "]";
    }

    @Override
    public String visitArrayLiteralExpr(Expr.ArrayLiteral expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < expr.elements.size(); i++) {
            sb.append(expr.elements.get(i).accept(this));
            if (i < expr.elements.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String visitObjectLiteralExpr(Expr.ObjectLiteral expr) {
        if (expr.keys.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < expr.keys.size(); i++) {
            String key = expr.keys.get(i);
            if (key == null) {
                sb.append("...").append(expr.values.get(i).accept(this));
            } else {
                sb.append(key).append(":").append(expr.values.get(i).accept(this));
            }
            if (i < expr.keys.size() - 1) sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitFunctionExpr(Expr.FunctionExpr expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("function");
        if (expr.name != null) sb.append(" ").append(expr.name.getLexeme());
        sb.append("(").append(formatParams(expr.params)).append("){");
        for (Stmt s : expr.body) sb.append(s.accept(this));
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitArrowFunctionExpr(Expr.ArrowFunction expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(formatParams(expr.params)).append(")=>");
        if (expr.expression != null) {
            sb.append(expr.expression.accept(this));
        } else {
            sb.append("{");
            for (Stmt s : expr.body) sb.append(s.accept(this));
            sb.append("}");
        }
        return sb.toString();
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return expr.condition.accept(this) + "?" + expr.thenBranch.accept(this)
                + ":" + expr.elseBranch.accept(this);
    }

    @Override
    public String visitSpreadExpr(Expr.Spread expr) {
        return "..." + expr.expression.accept(this);
    }

    @Override
    public String visitTemplateLiteralExpr(Expr.TemplateLiteral expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("`");
        for (int i = 0; i < expr.parts.size(); i++) {
            sb.append(expr.parts.get(i));
            if (i < expr.expressions.size()) {
                sb.append("${").append(expr.expressions.get(i).accept(this)).append("}");
            }
        }
        sb.append("`");
        return sb.toString();
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return "(" + expr.expression.accept(this) + ")";
    }

    @Override
    public String visitTypeofExpr(Expr.TypeofExpr expr) {
        return "typeof " + expr.operand.accept(this);
    }

    @Override
    public String visitNewExpr(Expr.New expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(expr.constructor.accept(this));
        if (expr.arguments != null) {
            sb.append("(");
            for (int i = 0; i < expr.arguments.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(expr.arguments.get(i).accept(this));
            }
            sb.append(")");
        }
        return sb.toString();
    }

    // ── Helper ──────────────────────────────────────────────────────────

    private String wrapInBlock(Stmt stmt) {
        if (stmt instanceof Stmt.Block) {
            return stmt.accept(this);
        }
        return "{" + stmt.accept(this) + "}";
    }
}
