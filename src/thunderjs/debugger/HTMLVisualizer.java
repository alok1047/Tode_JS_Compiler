package thunderjs.debugger;

public class HTMLVisualizer {

    public static String generate(ASTPrinter.TreeNode root) {
        StringBuilder treeHTML = new StringBuilder();
        toHTMLHelper(root, treeHTML);

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ThunderJS AST Visualization</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-color: #0f172a;
            --card-bg: #1e293b;
            --text-color: #f8fafc;
            --accent-color: #38bdf8;
            --border-color: #334155;
            --node-bg: #1e293b;
            --leaf-color: #94a3b8;
        }
        body {
            font-family: 'Inter', sans-serif;
            background-color: var(--bg-color);
            color: var(--text-color);
            margin: 0;
            padding: 2rem;
            display: flex;
            flex-direction: column;
            align-items: center;
            min-height: 100vh;
        }
        h1 {
            font-size: 2.25rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
            background: linear-gradient(to right, #38bdf8, #818cf8);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .subtitle {
            color: #64748b;
            margin-bottom: 2rem;
            font-family: 'JetBrains Mono', monospace;
        }
        .container {
            background-color: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 2rem;
            width: 100%;
            max-width: 800px;
            box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
        }
        ul {
            list-style-type: none;
            padding-left: 1.5rem;
            margin: 0.25rem 0;
            border-left: 1px dashed var(--border-color);
        }
        li {
            margin: 0.5rem 0;
            position: relative;
        }
        details > summary {
            list-style: none;
            cursor: pointer;
            outline: none;
            display: flex;
            align-items: center;
        }
        details > summary::-webkit-details-marker {
            display: none;
        }
        details > summary::before {
            content: "▼";
            display: inline-block;
            margin-right: 0.5rem;
            color: var(--accent-color);
            font-size: 0.8rem;
            transition: transform 0.2s ease;
        }
        details[open] > summary::before {
            transform: rotate(0deg);
        }
        details:not([open]) > summary::before {
            transform: rotate(-90deg);
        }
        .node-label {
            background-color: rgba(56, 189, 248, 0.1);
            border: 1px solid rgba(56, 189, 248, 0.2);
            color: var(--accent-color);
            padding: 0.25rem 0.5rem;
            border-radius: 6px;
            font-family: 'JetBrains Mono', monospace;
            font-size: 0.9rem;
            transition: all 0.2s ease;
        }
        .node-label:hover {
            background-color: rgba(56, 189, 248, 0.2);
        }
        .leaf .node-label {
            background-color: rgba(148, 163, 184, 0.05);
            border: 1px solid rgba(148, 163, 184, 0.1);
            color: var(--leaf-color);
        }
        .leaf::before {
            content: "•";
            color: var(--leaf-color);
            display: inline-block;
            width: 1rem;
            margin-left: -0.75rem;
        }
        .controls {
            margin-bottom: 1rem;
            display: flex;
            gap: 0.5rem;
        }
        button {
            background-color: #334155;
            color: #f8fafc;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 6px;
            cursor: pointer;
            font-size: 0.875rem;
            font-weight: 500;
            transition: background-color 0.2s ease;
        }
        button:hover {
            background-color: #475569;
        }
    </style>
</head>
<body>
    <h1>⚡ ThunderJS AST</h1>
    <div class="subtitle">Abstract Syntax Tree Visualization</div>
    <div class="controls">
        <button onclick="expandAll()">Expand All</button>
        <button onclick="collapseAll()">Collapse All</button>
    </div>
    <div class="container">
        <ul style="border-left: none; padding-left: 0;">
""" + treeHTML.toString() + """
        </ul>
    </div>
    <script>
        function expandAll() {
            document.querySelectorAll('details').forEach(el => el.open = true);
        }
        function collapseAll() {
            document.querySelectorAll('details').forEach(el => el.open = false);
        }
    </script>
</body>
</html>
""";
    }

    private static void toHTMLHelper(ASTPrinter.TreeNode node, StringBuilder sb) {
        if (node.children.isEmpty()) {
            sb.append("            <li class='leaf'><span class='node-label'>").append(escapeHTML(node.label)).append("</span></li>\n");
        } else {
            sb.append("            <li class='parent'><details open>\n");
            sb.append("                <summary><span class='node-label'>").append(escapeHTML(node.label)).append("</span></summary>\n");
            sb.append("                <ul>\n");
            for (ASTPrinter.TreeNode child : node.children) {
                toHTMLHelper(child, sb);
            }
            sb.append("                </ul>\n");
            sb.append("            </details></li>\n");
        }
    }

    private static String escapeHTML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#039;");
    }
}
