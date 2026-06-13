package thunderjs.debugger;

import java.util.Set;
import java.util.TreeSet;

public class CoverageTracker {
    private static final Set<String> ALL_NODES = new TreeSet<>(Set.of(
        // Stmt types
        "ExpressionStmt", "VarDeclaration", "Block", "If", "For", "While", "DoWhile", "FunctionDecl", "Return", "Switch", "Break", "Continue",
        // Expr types
        "Literal", "Identifier", "Binary", "Unary", "Logical", "Assign", "MemberAssign", "ComputedAssign", "CompoundAssign", "Update",
        "Call", "MemberAccess", "ComputedAccess", "ArrayLiteral", "ObjectLiteral", "FunctionExpr", "ArrowFunction", "Ternary", "Spread", "TemplateLiteral", "Grouping", "TypeofExpr"
    ));

    private final Set<String> visitedNodes = new TreeSet<>();
    private final boolean enabled;

    public CoverageTracker(boolean enabled) {
        this.enabled = enabled;
    }

    public void markVisited(Object node) {
        if (!enabled || node == null) return;
        String name = node.getClass().getSimpleName();
        if (ALL_NODES.contains(name)) {
            visitedNodes.add(name);
        }
    }

    public void printSummary() {
        if (!enabled) return;

        System.out.println("\n=== 📊 Feature Coverage Summary ===");
        System.out.println("Visited Node Types:");
        for (String node : visitedNodes) {
            System.out.println("  [✅] " + node);
        }

        System.out.println("\nUnvisited Node Types:");
        int unvisitedCount = 0;
        for (String node : ALL_NODES) {
            if (!visitedNodes.contains(node)) {
                System.out.println("  [❌] " + node);
                unvisitedCount++;
            }
        }

        double percentage = (double) visitedNodes.size() / ALL_NODES.size() * 100;
        System.out.println("---------------------------------");
        System.out.printf("Coverage: %.1f%% (%d/%d AST Node Types Visited)\n",
                percentage, visitedNodes.size(), ALL_NODES.size());
        System.out.println("=================================");
    }
}
