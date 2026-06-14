package tests;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StressTester {
    public static void main(String[] args) {
        File testsDir = new File("tests");
        if (!testsDir.exists() || !testsDir.isDirectory()) {
            System.err.println("Error: Could not find 'tests' directory in current path: " + new File(".").getAbsolutePath());
            System.exit(1);
        }

        List<File> jsFiles = new ArrayList<>();
        findJsFiles(testsDir, jsFiles);
        jsFiles.sort((f1, f2) -> f1.getPath().compareTo(f2.getPath()));

        int total = 0;
        int passed = 0;
        int failed = 0;

        StringBuilder report = new StringBuilder();
        report.append("# Hidden-Test Stress Suite Report — ThunderJS\n\n");
        report.append("This report details the execution results of the stress suite designed to validate execution correctness and find edge cases in the ThunderJS interpreter.\n\n");
        report.append("| Status | Test File | Expected | Actual | Details |\n");
        report.append("| --- | --- | --- | --- | --- |\n");

        System.out.println("⚡ Running Tode stress test suite...");
        System.out.println("================================================================================");

        for (File file : jsFiles) {
            String content;
            try {
                content = Files.readString(file.toPath());
            } catch (IOException e) {
                System.err.println("Failed to read: " + file.getPath());
                continue;
            }

            // Extract expectations
            String expectedOutput = null;
            String expectedError = null;

            int commentStart = content.indexOf("/*");
            int commentEnd = content.indexOf("*/", commentStart);
            if (commentStart != -1 && commentEnd != -1) {
                String comment = content.substring(commentStart + 2, commentEnd).trim();
                if (comment.startsWith("Expected:")) {
                    expectedOutput = comment.substring("Expected:".length()).trim();
                } else if (comment.startsWith("Expected Error:")) {
                    expectedError = comment.substring("Expected Error:".length()).trim();
                }
            }

            // If it doesn't specify expectations, skip it (e.g. TestRunner/StressTester/helper files)
            if (expectedOutput == null && expectedError == null) {
                continue;
            }

            total++;

            // Run process
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add("thunderjs.Main");
            command.add(file.getPath());
            command.add("--no-color");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // combine stdout and stderr

            String actualOutput = "";
            int exitCode = -1;
            boolean processSuccess = false;

            try {
                Process process = pb.start();
                
                // Read output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    actualOutput = sb.toString();
                }

                // Wait for process with a timeout of 5 seconds to prevent hanging tests
                if (process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    exitCode = process.exitValue();
                    processSuccess = true;
                } else {
                    process.destroyForcibly();
                    actualOutput += "\n[TIMEOUT] Process did not exit within 5 seconds.";
                }
            } catch (Exception e) {
                actualOutput = "Execution Error: " + e.getMessage();
            }

            // Verify
            boolean success = false;
            String detail = "";

            if (expectedError != null) {
                // We expect an error
                boolean hasErrorText = actualOutput.contains(expectedError);
                if (hasErrorText) {
                    success = true;
                } else {
                    detail = "Expected error type '" + expectedError + "' not found in output.";
                }
            } else {
                // We expect standard output
                String normActual = normalize(actualOutput);
                String normExpected = normalize(expectedOutput);
                if (exitCode == 0 && normActual.equals(normExpected)) {
                    success = true;
                } else if (exitCode != 0) {
                    detail = "Interpreter exited with code " + exitCode + ". Expected exit code 0.";
                } else {
                    detail = "Output mismatch.";
                }
            }

            // Get path relative to the workspace root for cleaner logging
            String relativePath = file.getPath();
            if (relativePath.startsWith("./")) {
                relativePath = relativePath.substring(2);
            }

            if (success) {
                passed++;
                System.out.printf("✅ PASS: %s\n", relativePath);
                report.append("| PASS ✅ | `").append(relativePath).append("` | `")
                      .append(escapeMarkdown(expectedError != null ? expectedError : expectedOutput))
                      .append("` | `")
                      .append(escapeMarkdown(actualOutput.trim()))
                      .append("` | |\n");
            } else {
                failed++;
                System.out.printf("❌ FAIL: %s\n", relativePath);
                if (expectedError != null) {
                    System.out.printf("   Expected Error: %s\n", expectedError);
                    System.out.printf("   Actual Output:   %s\n", actualOutput.trim().replace("\n", "\n   "));
                } else {
                    System.out.printf("   Expected Output: %s\n", expectedOutput.replace("\n", "\n   "));
                    System.out.printf("   Actual Output:   %s\n", actualOutput.trim().replace("\n", "\n   "));
                }
                if (!detail.isEmpty()) {
                    System.out.printf("   Reason:          %s\n", detail);
                }
                System.out.println("--------------------------------------------------------------------------------");
                report.append("| FAIL ❌ | `").append(relativePath).append("` | `")
                      .append(escapeMarkdown(expectedError != null ? expectedError : expectedOutput))
                      .append("` | `")
                      .append(escapeMarkdown(actualOutput.trim()))
                      .append("` | ")
                      .append(detail)
                      .append(" |\n");
            }
        }

        System.out.println("================================================================================");
        double rate = total > 0 ? ((double) passed / total) * 100 : 0.0;
        System.out.printf("Summary: %d passed, %d failed out of %d total (Success Rate: %.2f%%)\n", passed, failed, total, rate);

        report.append("\n## Final Summary\n");
        report.append("* **Total Tests:** ").append(total).append("\n");
        report.append("* **Passed:** ").append(passed).append("\n");
        report.append("* **Failed:** ").append(failed).append("\n");
        report.append("* **Success Rate:** ").append(String.format("%.2f", rate)).append("%\n");

        String reportPath = "tests/hidden_test_report.md";
        try {
            Files.writeString(Path.of(reportPath), report.toString());
        } catch (IOException e) {
            System.err.println("Failed to write report: " + e.getMessage());
        }
    }

    private static void findJsFiles(File dir, List<File> jsFiles) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    findJsFiles(f, jsFiles);
                } else if (f.getName().endsWith(".js")) {
                    jsFiles.add(f);
                }
            }
        }
    }

    private static String normalize(String text) {
        if (text == null) return "";
        return text.replace("\r\n", "\n").trim();
    }

    private static String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("\n", "<br>").replace("|", "\\|").replace("`", "\\`").trim();
    }
}
