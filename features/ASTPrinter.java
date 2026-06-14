package features;

import thunderjs.ast.Expr;
import thunderjs.ast.Stmt;
import thunderjs.lexer.Token;
import java.util.ArrayList;
import java.util.List;

public class ASTPrinter implements Expr.Visitor<ASTPrinter.TreeNode>, Stmt.Visitor<ASTPrinter.TreeNode> {

    public static class TreeNode {
        public String label;
        public List<TreeNode> children = new ArrayList<>();

        public TreeNode(String label) {
            this.label = label;
        }
    }

    public String print(List<Stmt> statements) {
        return formatTree(getRootNode(statements));
    }

    public TreeNode getRootNode(List<Stmt> statements) {
        TreeNode root = new TreeNode("🌳 ThunderJS AST\n\nProgram");
        for (Stmt stmt : statements) {
            TreeNode child = stmt.accept(this);
            if (child != null) root.children.add(child);
        }
        return root;
    }

    public static String formatTree(TreeNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.label).append("\n");
        formatTreeHelper(node, sb, "");
        return sb.toString();
    }

    private static void formatTreeHelper(TreeNode node, StringBuilder sb, String prefix) {
        for (int i = 0; i < node.children.size(); i++) {
            TreeNode child = node.children.get(i);
            boolean isLast = (i == node.children.size() - 1);
            sb.append(prefix);
            sb.append(isLast ? "└── " : "├── ");
            sb.append(child.label).append("\n");
            String childPrefix = prefix + (isLast ? "    " : "│   ");
            formatTreeHelper(child, sb, childPrefix);
        }
    }

    private TreeNode makeExprLeafNode(Expr expr) {
        if (expr instanceof Expr.Literal) {
            return new TreeNode("Literal(" + SourceCodeReconstructor.toSource(expr) + ")");
        }
        if (expr instanceof Expr.Identifier) {
            return new TreeNode("Identifier(" + SourceCodeReconstructor.toSource(expr) + ")");
        }
        if (expr instanceof Expr.Binary) {
            return new TreeNode("Binary(" + ((Expr.Binary) expr).operator.getLexeme() + ")");
        }
        return new TreeNode("Expression(" + SourceCodeReconstructor.toSource(expr) + ")");
    }

    // ════════════════════════════════════════════════════════════════════
    //  STATEMENT VISITORS
    // ════════════════════════════════════════════════════════════════════

    @Override
    public TreeNode visitExpressionStmt(Stmt.ExpressionStmt stmt) {
        return stmt.expression.accept(this);
    }

    @Override
    public TreeNode visitVarDeclarationStmt(Stmt.VarDeclaration stmt) {
        String label = stmt.keyword.getLexeme() + " " + stmt.name.getLexeme() +
            (stmt.initializer != null ? " = " + SourceCodeReconstructor.toSource(stmt.initializer) : "");
        TreeNode node = new TreeNode(label);
        if (stmt.initializer != null) {
            if (stmt.initializer instanceof Expr.Binary || stmt.initializer instanceof Expr.Logical) {
                TreeNode initNode = stmt.initializer.accept(this);
                node.children.addAll(initNode.children);
            } else {
                TreeNode initNode = stmt.initializer.accept(this);
                if (stmt.initializer instanceof Expr.ArrowFunction || stmt.initializer instanceof Expr.FunctionExpr) {
                    node.children.add(initNode);
                }
            }
        }
        return node;
    }

    @Override
    public TreeNode visitDestructuredVarDeclarationStmt(Stmt.DestructuredVarDeclaration stmt) {
        String label = stmt.keyword.getLexeme() + " " + SourceCodeReconstructor.toSource(stmt.pattern) +
            (stmt.initializer != null ? " = " + SourceCodeReconstructor.toSource(stmt.initializer) : "");
        TreeNode node = new TreeNode(label);
        if (stmt.initializer != null) {
            node.children.add(stmt.initializer.accept(this));
        }
        return node;
    }

    @Override
    public TreeNode visitForInStmt(Stmt.ForIn stmt) {
        TreeNode node = new TreeNode("ForIn");
        node.children.add(stmt.initializer.accept(this));
        node.children.add(stmt.object.accept(this));
        node.children.add(stmt.body.accept(this));
        return node;
    }

    @Override
    public TreeNode visitForOfStmt(Stmt.ForOf stmt) {
        TreeNode node = new TreeNode("ForOf");
        node.children.add(stmt.initializer.accept(this));
        node.children.add(stmt.iterable.accept(this));
        node.children.add(stmt.body.accept(this));
        return node;
    }

    @Override
    public TreeNode visitBlockStmt(Stmt.Block stmt) {
        TreeNode node = new TreeNode("Block");
        for (Stmt s : stmt.statements) {
            TreeNode child = s.accept(this);
            if (child != null) node.children.add(child);
        }
        return node;
    }

    @Override
    public TreeNode visitIfStmt(Stmt.If stmt) {
        TreeNode node = new TreeNode("if (" + SourceCodeReconstructor.toSource(stmt.condition) + ")");
        TreeNode thenNode = stmt.thenBranch.accept(this);
        if (thenNode != null) {
            node.children.add(thenNode);
        }
        if (stmt.elseBranch != null) {
            TreeNode elseNode = stmt.elseBranch.accept(this);
            if (elseNode != null) {
                node.children.add(elseNode);
            }
        }
        return node;
    }

    @Override
    public TreeNode visitForStmt(Stmt.For stmt) {
        String initStr = stmt.initializer != null ? SourceCodeReconstructor.toSource(stmt.initializer) : "";
        String condStr = stmt.condition != null ? SourceCodeReconstructor.toSource(stmt.condition) : "";
        String incrStr = stmt.increment != null ? SourceCodeReconstructor.toSource(stmt.increment) : "";
        TreeNode node = new TreeNode("for (" + initStr + "; " + condStr + "; " + incrStr + ")");
        TreeNode bodyNode = stmt.body.accept(this);
        if (bodyNode != null) {
            if (stmt.body instanceof Stmt.Block) {
                node.children.addAll(bodyNode.children);
            } else {
                node.children.add(bodyNode);
            }
        }
        return node;
    }

    @Override
    public TreeNode visitWhileStmt(Stmt.While stmt) {
        TreeNode node = new TreeNode("while (" + SourceCodeReconstructor.toSource(stmt.condition) + ")");
        TreeNode bodyNode = stmt.body.accept(this);
        if (bodyNode != null) {
            if (stmt.body instanceof Stmt.Block) {
                node.children.addAll(bodyNode.children);
            } else {
                node.children.add(bodyNode);
            }
        }
        return node;
    }

    @Override
    public TreeNode visitDoWhileStmt(Stmt.DoWhile stmt) {
        TreeNode node = new TreeNode("do ... while (" + SourceCodeReconstructor.toSource(stmt.condition) + ")");
        TreeNode bodyNode = stmt.body.accept(this);
        if (bodyNode != null) {
            if (stmt.body instanceof Stmt.Block) {
                node.children.addAll(bodyNode.children);
            } else {
                node.children.add(bodyNode);
            }
        }
        return node;
    }

    @Override
    public TreeNode visitFunctionDeclStmt(Stmt.FunctionDecl stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("function ").append(stmt.name.getLexeme()).append("(");
        for (int i = 0; i < stmt.params.size(); i++) {
            Expr.Parameter p = stmt.params.get(i);
            sb.append(p.isRest ? "..." : "").append(p.name.getLexeme());
            if (i < stmt.params.size() - 1) sb.append(", ");
        }
        sb.append(")");
        TreeNode node = new TreeNode(sb.toString());
        for (Stmt s : stmt.body) {
            TreeNode child = s.accept(this);
            if (child != null) node.children.add(child);
        }
        return node;
    }

    @Override
    public TreeNode visitReturnStmt(Stmt.Return stmt) {
        String label = "return" + (stmt.value != null ? " " + SourceCodeReconstructor.toSource(stmt.value) : "");
        TreeNode node = new TreeNode(label);
        if (stmt.value != null) {
            if (stmt.value instanceof Expr.Binary || stmt.value instanceof Expr.Logical) {
                TreeNode valNode = stmt.value.accept(this);
                node.children.addAll(valNode.children);
            } else if (stmt.value instanceof Expr.ArrowFunction || stmt.value instanceof Expr.FunctionExpr) {
                node.children.add(stmt.value.accept(this));
            }
        }
        return node;
    }

    @Override
    public TreeNode visitSwitchStmt(Stmt.Switch stmt) {
        TreeNode node = new TreeNode("switch (" + SourceCodeReconstructor.toSource(stmt.discriminant) + ")");
        for (Stmt.Case c : stmt.cases) {
            String label = c.value != null ? "case " + SourceCodeReconstructor.toSource(c.value) : "default";
            TreeNode caseNode = new TreeNode(label);
            for (Stmt s : c.body) {
                TreeNode child = s.accept(this);
                if (child != null) caseNode.children.add(child);
            }
            node.children.add(caseNode);
        }
        return node;
    }

    @Override
    public TreeNode visitBreakStmt(Stmt.Break stmt) {
        return new TreeNode("break");
    }

    @Override
    public TreeNode visitContinueStmt(Stmt.Continue stmt) {
        return new TreeNode("continue");
    }

    // ════════════════════════════════════════════════════════════════════
    //  EXPRESSION VISITORS
    // ════════════════════════════════════════════════════════════════════

    @Override
    public TreeNode visitLiteralExpr(Expr.Literal expr) {
        return new TreeNode("Literal(" + SourceCodeReconstructor.toSource(expr) + ")");
    }

    @Override
    public TreeNode visitIdentifierExpr(Expr.Identifier expr) {
        return new TreeNode("Identifier(" + SourceCodeReconstructor.toSource(expr) + ")");
    }

    @Override
    public TreeNode visitBinaryExpr(Expr.Binary expr) {
        TreeNode node = new TreeNode(SourceCodeReconstructor.toSource(expr));
        node.children.add(makeExprLeafNode(expr.left));
        node.children.add(new TreeNode("Operator(" + expr.operator.getLexeme() + ")"));
        node.children.add(makeExprLeafNode(expr.right));
        return node;
    }

    @Override
    public TreeNode visitUnaryExpr(Expr.Unary expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitLogicalExpr(Expr.Logical expr) {
        TreeNode node = new TreeNode(SourceCodeReconstructor.toSource(expr));
        node.children.add(makeExprLeafNode(expr.left));
        node.children.add(new TreeNode("Operator(" + expr.operator.getLexeme() + ")"));
        node.children.add(makeExprLeafNode(expr.right));
        return node;
    }

    @Override
    public TreeNode visitAssignExpr(Expr.Assign expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitDestructuredAssignExpr(Expr.DestructuredAssign expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitDeleteExpr(Expr.DeleteExpr expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitDefaultValExpr(Expr.DefaultVal expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitMemberAssignExpr(Expr.MemberAssign expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitComputedAssignExpr(Expr.ComputedAssign expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitCompoundAssignExpr(Expr.CompoundAssign expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitUpdateExpr(Expr.Update expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitCallExpr(Expr.Call expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitMemberAccessExpr(Expr.MemberAccess expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitComputedAccessExpr(Expr.ComputedAccess expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitArrayLiteralExpr(Expr.ArrayLiteral expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitObjectLiteralExpr(Expr.ObjectLiteral expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitFunctionExpr(Expr.FunctionExpr expr) {
        String name = expr.name != null ? expr.name.getLexeme() : "anonymous";
        TreeNode node = new TreeNode("function " + name);
        for (Stmt s : expr.body) {
            TreeNode child = s.accept(this);
            if (child != null) node.children.add(child);
        }
        return node;
    }

    @Override
    public TreeNode visitArrowFunctionExpr(Expr.ArrowFunction expr) {
        TreeNode node = new TreeNode(SourceCodeReconstructor.toSource(expr));
        if (expr.expression != null) {
            if (expr.expression instanceof Expr.Binary || expr.expression instanceof Expr.Logical) {
                TreeNode expNode = expr.expression.accept(this);
                node.children.addAll(expNode.children);
            } else {
                node.children.add(makeExprLeafNode(expr.expression));
            }
        } else {
            for (Stmt s : expr.body) {
                TreeNode child = s.accept(this);
                if (child != null) node.children.add(child);
            }
        }
        return node;
    }

    @Override
    public TreeNode visitTernaryExpr(Expr.Ternary expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitSpreadExpr(Expr.Spread expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitTemplateLiteralExpr(Expr.TemplateLiteral expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public TreeNode visitTypeofExpr(Expr.TypeofExpr expr) {
        return new TreeNode(SourceCodeReconstructor.toSource(expr));
    }

    @Override
    public TreeNode visitNewExpr(Expr.New expr) {
        TreeNode node = new TreeNode("new " + SourceCodeReconstructor.toSource(expr.constructor));
        if (expr.arguments != null) {
            for (Expr arg : expr.arguments) {
                node.children.add(makeExprLeafNode(arg));
            }
        }
        return node;
    }

    // ════════════════════════════════════════════════════════════════════
    //  SOURCE CODE RECONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════

    public static class SourceCodeReconstructor implements Expr.Visitor<String>, Stmt.Visitor<String> {
        public static String toSource(Stmt stmt) {
            if (stmt == null) return "";
            return stmt.accept(new SourceCodeReconstructor());
        }

        public static String toSource(Expr expr) {
            if (expr == null) return "";
            return expr.accept(new SourceCodeReconstructor());
        }

        @Override
        public String visitExpressionStmt(Stmt.ExpressionStmt stmt) {
            return toSource(stmt.expression);
        }

        @Override
        public String visitVarDeclarationStmt(Stmt.VarDeclaration stmt) {
            return stmt.keyword.getLexeme() + " " + stmt.name.getLexeme() +
                (stmt.initializer != null ? " = " + toSource(stmt.initializer) : "");
        }

        @Override
        public String visitDestructuredVarDeclarationStmt(Stmt.DestructuredVarDeclaration stmt) {
            return stmt.keyword.getLexeme() + " " + toSource(stmt.pattern) +
                (stmt.initializer != null ? " = " + toSource(stmt.initializer) : "");
        }

        @Override
        public String visitForInStmt(Stmt.ForIn stmt) {
            return "for (" + toSource(stmt.initializer) + " in " + toSource(stmt.object) + ") " + toSource(stmt.body);
        }

        @Override
        public String visitForOfStmt(Stmt.ForOf stmt) {
            return "for (" + toSource(stmt.initializer) + " of " + toSource(stmt.iterable) + ") " + toSource(stmt.body);
        }

        @Override
        public String visitBlockStmt(Stmt.Block stmt) {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            for (Stmt s : stmt.statements) {
                sb.append(toSource(s)).append("; ");
            }
            sb.append("}");
            return sb.toString();
        }

        @Override
        public String visitIfStmt(Stmt.If stmt) {
            return "if (" + toSource(stmt.condition) + ") " + toSource(stmt.thenBranch) +
                (stmt.elseBranch != null ? " else " + toSource(stmt.elseBranch) : "");
        }

        @Override
        public String visitForStmt(Stmt.For stmt) {
            return "for (" +
                (stmt.initializer != null ? toSource(stmt.initializer) : "") + "; " +
                (stmt.condition != null ? toSource(stmt.condition) : "") + "; " +
                (stmt.increment != null ? toSource(stmt.increment) : "") + ") " +
                toSource(stmt.body);
        }

        @Override
        public String visitWhileStmt(Stmt.While stmt) {
            return "while (" + toSource(stmt.condition) + ") " + toSource(stmt.body);
        }

        @Override
        public String visitDoWhileStmt(Stmt.DoWhile stmt) {
            return "do " + toSource(stmt.body) + " while (" + toSource(stmt.condition) + ")";
        }

        @Override
        public String visitFunctionDeclStmt(Stmt.FunctionDecl stmt) {
            StringBuilder sb = new StringBuilder();
            sb.append("function ").append(stmt.name.getLexeme()).append("(");
            for (int i = 0; i < stmt.params.size(); i++) {
                Expr.Parameter p = stmt.params.get(i);
                sb.append(p.isRest ? "..." : "").append(p.name.getLexeme());
                if (i < stmt.params.size() - 1) sb.append(", ");
            }
            sb.append(") { ... }");
            return sb.toString();
        }

        @Override
        public String visitReturnStmt(Stmt.Return stmt) {
            return "return" + (stmt.value != null ? " " + toSource(stmt.value) : "");
        }

        @Override
        public String visitSwitchStmt(Stmt.Switch stmt) {
            return "switch (" + toSource(stmt.discriminant) + ") { ... }";
        }

        @Override
        public String visitBreakStmt(Stmt.Break stmt) {
            return "break";
        }

        @Override
        public String visitContinueStmt(Stmt.Continue stmt) {
            return "continue";
        }

        @Override
        public String visitLiteralExpr(Expr.Literal expr) {
            if (expr.value == null) return "null";
            if (expr.value instanceof String) {
                return "\"" + expr.value.toString().replace("\"", "\\\"") + "\"";
            }
            if (expr.value instanceof Double) {
                double d = (Double) expr.value;
                if (d == (long) d) {
                    return String.format("%d", (long) d);
                }
                return d + "";
            }
            return expr.value.toString();
        }

        @Override
        public String visitIdentifierExpr(Expr.Identifier expr) {
            return expr.name.getLexeme();
        }

        @Override
        public String visitBinaryExpr(Expr.Binary expr) {
            return toSource(expr.left) + " " + expr.operator.getLexeme() + " " + toSource(expr.right);
        }

        @Override
        public String visitUnaryExpr(Expr.Unary expr) {
            return expr.operator.getLexeme() + toSource(expr.operand);
        }

        @Override
        public String visitLogicalExpr(Expr.Logical expr) {
            return toSource(expr.left) + " " + expr.operator.getLexeme() + " " + toSource(expr.right);
        }

        @Override
        public String visitAssignExpr(Expr.Assign expr) {
            return expr.name.getLexeme() + " = " + toSource(expr.value);
        }

        @Override
        public String visitDestructuredAssignExpr(Expr.DestructuredAssign expr) {
            return toSource(expr.pattern) + " = " + toSource(expr.value);
        }

        @Override
        public String visitDeleteExpr(Expr.DeleteExpr expr) {
            return "delete " + toSource(expr.operand);
        }

        @Override
        public String visitDefaultValExpr(Expr.DefaultVal expr) {
            return toSource(expr.target) + " = " + toSource(expr.defaultValue);
        }

        @Override
        public String visitMemberAssignExpr(Expr.MemberAssign expr) {
            return toSource(expr.object) + "." + expr.name.getLexeme() + " = " + toSource(expr.value);
        }

        @Override
        public String visitComputedAssignExpr(Expr.ComputedAssign expr) {
            return toSource(expr.object) + "[" + toSource(expr.index) + "] = " + toSource(expr.value);
        }

        @Override
        public String visitCompoundAssignExpr(Expr.CompoundAssign expr) {
            return toSource(expr.target) + " " + expr.operator.getLexeme() + " " + toSource(expr.value);
        }

        @Override
        public String visitUpdateExpr(Expr.Update expr) {
            if (expr.isPrefix) {
                return expr.operator.getLexeme() + toSource(expr.target);
            } else {
                return toSource(expr.target) + expr.operator.getLexeme();
            }
        }

        @Override
        public String visitCallExpr(Expr.Call expr) {
            StringBuilder sb = new StringBuilder();
            sb.append(toSource(expr.callee)).append("(");
            for (int i = 0; i < expr.arguments.size(); i++) {
                sb.append(toSource(expr.arguments.get(i)));
                if (i < expr.arguments.size() - 1) sb.append(", ");
            }
            sb.append(")");
            return sb.toString();
        }

        @Override
        public String visitMemberAccessExpr(Expr.MemberAccess expr) {
            return toSource(expr.object) + "." + expr.name.getLexeme();
        }

        @Override
        public String visitComputedAccessExpr(Expr.ComputedAccess expr) {
            return toSource(expr.object) + "[" + toSource(expr.index) + "]";
        }

        @Override
        public String visitArrayLiteralExpr(Expr.ArrayLiteral expr) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < expr.elements.size(); i++) {
                sb.append(toSource(expr.elements.get(i)));
                if (i < expr.elements.size() - 1) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }

        @Override
        public String visitObjectLiteralExpr(Expr.ObjectLiteral expr) {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            for (int i = 0; i < expr.keys.size(); i++) {
                Object key = expr.keys.get(i);
                if (key == null) {
                    sb.append("...").append(toSource(expr.values.get(i)));
                } else if (key instanceof Expr computed) {
                    sb.append("[").append(toSource(computed)).append("]: ").append(toSource(expr.values.get(i)));
                } else {
                    sb.append(key).append(": ").append(toSource(expr.values.get(i)));
                }
                if (i < expr.keys.size() - 1) sb.append(", ");
            }
            sb.append(" }");
            return sb.toString();
        }

        @Override
        public String visitFunctionExpr(Expr.FunctionExpr expr) {
            StringBuilder sb = new StringBuilder();
            sb.append("function ").append(expr.name != null ? expr.name.getLexeme() : "").append("(");
            for (int i = 0; i < expr.params.size(); i++) {
                Expr.Parameter p = expr.params.get(i);
                sb.append(p.isRest ? "..." : "").append(p.name.getLexeme());
                if (i < expr.params.size() - 1) sb.append(", ");
            }
            sb.append(") { ... }");
            return sb.toString();
        }

        @Override
        public String visitArrowFunctionExpr(Expr.ArrowFunction expr) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < expr.params.size(); i++) {
                Expr.Parameter p = expr.params.get(i);
                sb.append(p.isRest ? "..." : "").append(p.name.getLexeme());
                if (i < expr.params.size() - 1) sb.append(", ");
            }
            sb.append(") => ");
            if (expr.expression != null) {
                sb.append(toSource(expr.expression));
            } else {
                sb.append("{ ... }");
            }
            return sb.toString();
        }

        @Override
        public String visitTernaryExpr(Expr.Ternary expr) {
            return toSource(expr.condition) + " ? " + toSource(expr.thenBranch) + " : " + toSource(expr.elseBranch);
        }

        @Override
        public String visitSpreadExpr(Expr.Spread expr) {
            return "..." + toSource(expr.expression);
        }

        @Override
        public String visitTemplateLiteralExpr(Expr.TemplateLiteral expr) {
            StringBuilder sb = new StringBuilder();
            sb.append("`");
            for (int i = 0; i < expr.parts.size(); i++) {
                sb.append(expr.parts.get(i));
                if (i < expr.expressions.size()) {
                    sb.append("${").append(toSource(expr.expressions.get(i))).append("}");
                }
            }
            sb.append("`");
            return sb.toString();
        }

        @Override
        public String visitGroupingExpr(Expr.Grouping expr) {
            return "(" + toSource(expr.expression) + ")";
        }

        @Override
        public String visitTypeofExpr(Expr.TypeofExpr expr) {
            return "typeof " + toSource(expr.operand);
        }

        @Override
        public String visitNewExpr(Expr.New expr) {
            StringBuilder sb = new StringBuilder();
            sb.append("new ").append(toSource(expr.constructor));
            if (expr.arguments != null) {
                sb.append("(");
                for (int i = 0; i < expr.arguments.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(toSource(expr.arguments.get(i)));
                }
                sb.append(")");
            }
            return sb.toString();
        }
    }
}
