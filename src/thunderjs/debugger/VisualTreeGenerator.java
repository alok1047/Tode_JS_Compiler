package thunderjs.debugger;

import thunderjs.ast.Expr;
import thunderjs.ast.Stmt;
import java.util.ArrayList;
import java.util.List;

public class VisualTreeGenerator {

    public static String generate(List<Stmt> statements) {
        List<String> variables = new ArrayList<>();
        List<String> expressions = new ArrayList<>();
        List<String> outputs = new ArrayList<>();

        collect(statements, variables, expressions, outputs);

        ASTPrinter.TreeNode root = new ASTPrinter.TreeNode("🌳 ThunderJS Visual Tree\n\nProgram");

        if (!variables.isEmpty()) {
            ASTPrinter.TreeNode varNode = new ASTPrinter.TreeNode("Variables");
            for (String var : variables) {
                varNode.children.add(new ASTPrinter.TreeNode(var));
            }
            root.children.add(varNode);
        }

        if (!expressions.isEmpty()) {
            ASTPrinter.TreeNode expNode = new ASTPrinter.TreeNode("Expressions");
            for (String exp : expressions) {
                expNode.children.add(new ASTPrinter.TreeNode(exp));
            }
            root.children.add(expNode);
        }

        if (!outputs.isEmpty()) {
            ASTPrinter.TreeNode outNode = new ASTPrinter.TreeNode("Output");
            for (String out : outputs) {
                outNode.children.add(new ASTPrinter.TreeNode(out));
            }
            root.children.add(outNode);
        }

        return ASTPrinter.formatTree(root);
    }

    private static void collect(List<Stmt> statements, List<String> variables, List<String> expressions, List<String> outputs) {
        for (Stmt stmt : statements) {
            collect(stmt, variables, expressions, outputs);
        }
    }

    private static void collect(Stmt stmt, List<String> variables, List<String> expressions, List<String> outputs) {
        if (stmt == null) return;
        if (stmt instanceof Stmt.VarDeclaration varDecl) {
            variables.add(varDecl.name.getLexeme());
            if (varDecl.initializer != null) {
                collectExpr(varDecl.initializer, expressions);
            }
        } else if (stmt instanceof Stmt.ExpressionStmt exprStmt) {
            Expr expr = exprStmt.expression;
            if (expr instanceof Expr.Call call && isConsoleLog(call)) {
                outputs.add(ASTPrinter.SourceCodeReconstructor.toSource(call));
                for (Expr arg : call.arguments) {
                    collectExpr(arg, expressions);
                }
            } else {
                collectExpr(expr, expressions);
            }
        } else if (stmt instanceof Stmt.Block block) {
            collect(block.statements, variables, expressions, outputs);
        } else if (stmt instanceof Stmt.If ifStmt) {
            collectExpr(ifStmt.condition, expressions);
            collect(ifStmt.thenBranch, variables, expressions, outputs);
            collect(ifStmt.elseBranch, variables, expressions, outputs);
        } else if (stmt instanceof Stmt.For forStmt) {
            collect(forStmt.initializer, variables, expressions, outputs);
            collectExpr(forStmt.condition, expressions);
            collectExpr(forStmt.increment, expressions);
            collect(forStmt.body, variables, expressions, outputs);
        } else if (stmt instanceof Stmt.While whileStmt) {
            collectExpr(whileStmt.condition, expressions);
            collect(whileStmt.body, variables, expressions, outputs);
        } else if (stmt instanceof Stmt.DoWhile doWhileStmt) {
            collect(doWhileStmt.body, variables, expressions, outputs);
            collectExpr(doWhileStmt.condition, expressions);
        } else if (stmt instanceof Stmt.FunctionDecl fnDecl) {
            variables.add(fnDecl.name.getLexeme() + "()");
            collect(fnDecl.body, variables, expressions, outputs);
        } else if (stmt instanceof Stmt.Return retStmt) {
            if (retStmt.value != null) {
                collectExpr(retStmt.value, expressions);
            }
        } else if (stmt instanceof Stmt.Switch swStmt) {
            collectExpr(swStmt.discriminant, expressions);
            for (Stmt.Case c : swStmt.cases) {
                collect(c.body, variables, expressions, outputs);
            }
        }
    }

    private static void collectExpr(Expr expr, List<String> expressions) {
        if (expr == null) return;
        if (expr instanceof Expr.Binary || expr instanceof Expr.Logical || expr instanceof Expr.Ternary) {
            String src = ASTPrinter.SourceCodeReconstructor.toSource(expr);
            if (!expressions.contains(src)) {
                expressions.add(src);
            }
        }
        if (expr instanceof Expr.Binary bin) {
            collectExpr(bin.left, expressions);
            collectExpr(bin.right, expressions);
        } else if (expr instanceof Expr.Logical log) {
            collectExpr(log.left, expressions);
            collectExpr(log.right, expressions);
        } else if (expr instanceof Expr.Call call) {
            for (Expr arg : call.arguments) {
                collectExpr(arg, expressions);
            }
        } else if (expr instanceof Expr.ArrowFunction arrow) {
            if (arrow.expression != null) {
                collectExpr(arrow.expression, expressions);
            }
        }
    }

    private static boolean isConsoleLog(Expr.Call call) {
        if (call.callee instanceof Expr.MemberAccess member) {
            if (member.name.getLexeme().equals("log")) {
                if (member.object instanceof Expr.Identifier id) {
                    return id.name.getLexeme().equals("console");
                }
            }
        }
        return false;
    }
}
