package features;

import thunderjs.ast.Expr;
import thunderjs.ast.Stmt;
import thunderjs.util.Stringify;

import java.util.List;

/**
 * AST-based JavaScript pretty-printer / code formatter.
 *
 * Walks the parsed AST and emits cleanly indented, consistently
 * styled JavaScript source code. Useful for normalizing messy or
 * inconsistent code into a canonical readable form.
 */
public class Formatter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    private int indentLevel = 0;
    private static final String INDENT = "    "; // 4 spaces

    /**
     * Format a list of statements into a complete, pretty-printed source string.
     */
    public String format(List<Stmt> statements) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < statements.size(); i++) {
            String formatted = statements.get(i).accept(this);
            sb.append(formatted);
            if (i < statements.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    private String indent() {
        return INDENT.repeat(indentLevel);
    }

    private String formatBlock(List<Stmt> stmts) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        indentLevel++;
        for (Stmt s : stmts) {
            sb.append(s.accept(this));
        }
        indentLevel--;
        sb.append(indent()).append("}");
        return sb.toString();
    }

    private String formatParams(List<Expr.Parameter> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            Expr.Parameter p = params.get(i);
            if (p.isRest) sb.append("...");
            sb.append(p.name.getLexeme());
            if (p.defaultValue != null) {
                sb.append(" = ").append(p.defaultValue.accept(this));
            }
            if (i < params.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════════════
    //  STATEMENT VISITORS
    // ════════════════════════════════════════════════════════════════════

    @Override
    public String visitExpressionStmt(Stmt.ExpressionStmt stmt) {
        return indent() + stmt.expression.accept(this) + ";\n";
    }

    @Override
    public String visitVarDeclarationStmt(Stmt.VarDeclaration stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append(stmt.keyword.getLexeme()).append(" ").append(stmt.name.getLexeme());
        if (stmt.initializer != null) {
            sb.append(" = ").append(stmt.initializer.accept(this));
        }
        sb.append(";\n");
        return sb.toString();
    }

    @Override
    public String visitDestructuredVarDeclarationStmt(Stmt.DestructuredVarDeclaration stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append(stmt.keyword.getLexeme()).append(" ").append(stmt.pattern.accept(this));
        if (stmt.initializer != null) {
            sb.append(" = ").append(stmt.initializer.accept(this));
        }
        sb.append(";\n");
        return sb.toString();
    }

    @Override
    public String visitForInStmt(Stmt.ForIn stmt) {
        String init = stmt.initializer.accept(this).trim();
        if (init.endsWith(";")) {
            init = init.substring(0, init.length() - 1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("for (").append(init).append(" in ").append(stmt.object.accept(this)).append(") ");
        if (stmt.body instanceof Stmt.Block block) {
            sb.append(formatBlock(block.statements));
        } else {
            sb.append("\n");
            indentLevel++;
            sb.append(stmt.body.accept(this));
            indentLevel--;
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String visitForOfStmt(Stmt.ForOf stmt) {
        String init = stmt.initializer.accept(this).trim();
        if (init.endsWith(";")) {
            init = init.substring(0, init.length() - 1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("for (").append(init).append(" of ").append(stmt.iterable.accept(this)).append(") ");
        if (stmt.body instanceof Stmt.Block block) {
            sb.append(formatBlock(block.statements));
        } else {
            sb.append("\n");
            indentLevel++;
            sb.append(stmt.body.accept(this));
            indentLevel--;
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        return indent() + formatBlock(stmt.statements) + "\n";
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("if (").append(stmt.condition.accept(this)).append(") ");
        if (stmt.thenBranch instanceof Stmt.Block block) {
            sb.append(formatBlock(block.statements));
        } else {
            sb.append("{\n");
            indentLevel++;
            sb.append(stmt.thenBranch.accept(this));
            indentLevel--;
            sb.append(indent()).append("}");
        }
        if (stmt.elseBranch != null) {
            if (stmt.elseBranch instanceof Stmt.If) {
                sb.append(" else ");
                // Remove leading indent from the nested if
                String elseStr = stmt.elseBranch.accept(this);
                sb.append(elseStr.stripLeading());
            } else if (stmt.elseBranch instanceof Stmt.Block elseBlock) {
                sb.append(" else ").append(formatBlock(elseBlock.statements));
            } else {
                sb.append(" else {\n");
                indentLevel++;
                sb.append(stmt.elseBranch.accept(this));
                indentLevel--;
                sb.append(indent()).append("}");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String visitForStmt(Stmt.For stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("for (");
        if (stmt.initializer != null) {
            String init = stmt.initializer.accept(this).strip();
            // Remove trailing semicolon if present from VarDeclaration
            if (init.endsWith(";")) init = init.substring(0, init.length() - 1);
            sb.append(init);
        }
        sb.append("; ");
        if (stmt.condition != null) sb.append(stmt.condition.accept(this));
        sb.append("; ");
        if (stmt.increment != null) sb.append(stmt.increment.accept(this));
        sb.append(") ");
        if (stmt.body instanceof Stmt.Block block) {
            sb.append(formatBlock(block.statements));
        } else {
            sb.append("{\n");
            indentLevel++;
            sb.append(stmt.body.accept(this));
            indentLevel--;
            sb.append(indent()).append("}");
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("while (").append(stmt.condition.accept(this)).append(") ");
        if (stmt.body instanceof Stmt.Block block) {
            sb.append(formatBlock(block.statements));
        } else {
            sb.append("{\n");
            indentLevel++;
            sb.append(stmt.body.accept(this));
            indentLevel--;
            sb.append(indent()).append("}");
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String visitDoWhileStmt(Stmt.DoWhile stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("do ");
        if (stmt.body instanceof Stmt.Block block) {
            sb.append(formatBlock(block.statements));
        } else {
            sb.append("{\n");
            indentLevel++;
            sb.append(stmt.body.accept(this));
            indentLevel--;
            sb.append(indent()).append("}");
        }
        sb.append(" while (").append(stmt.condition.accept(this)).append(");\n");
        return sb.toString();
    }

    @Override
    public String visitFunctionDeclStmt(Stmt.FunctionDecl stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("function ").append(stmt.name.getLexeme());
        sb.append("(").append(formatParams(stmt.params)).append(") ");
        sb.append(formatBlock(stmt.body));
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("return");
        if (stmt.value != null) {
            sb.append(" ").append(stmt.value.accept(this));
        }
        sb.append(";\n");
        return sb.toString();
    }

    @Override
    public String visitSwitchStmt(Stmt.Switch stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("switch (").append(stmt.discriminant.accept(this)).append(") {\n");
        indentLevel++;
        for (Stmt.Case c : stmt.cases) {
            if (c.value != null) {
                sb.append(indent()).append("case ").append(c.value.accept(this)).append(":\n");
            } else {
                sb.append(indent()).append("default:\n");
            }
            indentLevel++;
            for (Stmt s : c.body) {
                sb.append(s.accept(this));
            }
            indentLevel--;
        }
        indentLevel--;
        sb.append(indent()).append("}\n");
        return sb.toString();
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return indent() + "break;\n";
    }

    @Override
    public String visitContinueStmt(Stmt.Continue stmt) {
        return indent() + "continue;\n";
    }

    // ════════════════════════════════════════════════════════════════════
    //  EXPRESSION VISITORS
    // ════════════════════════════════════════════════════════════════════

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "null";
        if (expr.value instanceof String s) {
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                          .replace("\n", "\\n").replace("\r", "\\r")
                          .replace("\t", "\\t") + "\"";
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
        return expr.left.accept(this) + " " + expr.operator.getLexeme() + " " + expr.right.accept(this);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return expr.operator.getLexeme() + expr.operand.accept(this);
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return expr.left.accept(this) + " " + expr.operator.getLexeme() + " " + expr.right.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return expr.name.getLexeme() + " = " + expr.value.accept(this);
    }

    @Override
    public String visitDestructuredAssignExpr(Expr.DestructuredAssign expr) {
        return expr.pattern.accept(this) + " = " + expr.value.accept(this);
    }

    @Override
    public String visitDeleteExpr(Expr.DeleteExpr expr) {
        return "delete " + expr.operand.accept(this);
    }

    @Override
    public String visitDefaultValExpr(Expr.DefaultVal expr) {
        return expr.target.accept(this) + " = " + expr.defaultValue.accept(this);
    }

    @Override
    public String visitMemberAssignExpr(Expr.MemberAssign expr) {
        return expr.object.accept(this) + "." + expr.name.getLexeme() + " = " + expr.value.accept(this);
    }

    @Override
    public String visitComputedAssignExpr(Expr.ComputedAssign expr) {
        return expr.object.accept(this) + "[" + expr.index.accept(this) + "] = " + expr.value.accept(this);
    }

    @Override
    public String visitCompoundAssignExpr(Expr.CompoundAssign expr) {
        return expr.target.accept(this) + " " + expr.operator.getLexeme() + " " + expr.value.accept(this);
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
            if (i < expr.arguments.size() - 1) sb.append(", ");
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
            if (i < expr.elements.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String visitObjectLiteralExpr(Expr.ObjectLiteral expr) {
        if (expr.keys.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (int i = 0; i < expr.keys.size(); i++) {
            Object key = expr.keys.get(i);
            if (key == null) {
                sb.append("...").append(expr.values.get(i).accept(this));
            } else if (key instanceof Expr keyExpr) {
                sb.append("[").append(keyExpr.accept(this)).append("]: ").append(expr.values.get(i).accept(this));
            } else {
                Expr value = expr.values.get(i);
                if (value instanceof Expr.Identifier id && id.name.getLexeme().equals(key)) {
                    sb.append(key);
                } else {
                    sb.append((String) key).append(": ").append(value.accept(this));
                }
            }
            if (i < expr.keys.size() - 1) sb.append(", ");
        }
        sb.append(" }");
        return sb.toString();
    }

    @Override
    public String visitFunctionExpr(Expr.FunctionExpr expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("function");
        if (expr.name != null) sb.append(" ").append(expr.name.getLexeme());
        sb.append("(").append(formatParams(expr.params)).append(") ");
        sb.append(formatBlock(expr.body));
        return sb.toString();
    }

    @Override
    public String visitArrowFunctionExpr(Expr.ArrowFunction expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(formatParams(expr.params)).append(") => ");
        if (expr.expression != null) {
            sb.append(expr.expression.accept(this));
        } else {
            sb.append(formatBlock(expr.body));
        }
        return sb.toString();
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return expr.condition.accept(this) + " ? " + expr.thenBranch.accept(this)
                + " : " + expr.elseBranch.accept(this);
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
                if (i > 0) sb.append(", ");
                sb.append(expr.arguments.get(i).accept(this));
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
