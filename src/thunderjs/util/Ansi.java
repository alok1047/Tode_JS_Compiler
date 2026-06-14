package thunderjs.util;

/**
 * Utility class for ANSI escape sequences to color terminal output.
 */
public final class Ansi {
    public static boolean enabled = (System.console() != null);

    public static final String RESET  = "\u001B[0m";

    public static final String RED    = "\u001B[31m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE   = "\u001B[34m";
    public static final String CYAN   = "\u001B[36m";
    public static final String GRAY   = "\u001B[90m";

    public static String red(String text) {
        return enabled ? RED + text + RESET : text;
    }

    public static String green(String text) {
        return enabled ? GREEN + text + RESET : text;
    }

    public static String yellow(String text) {
        return enabled ? YELLOW + text + RESET : text;
    }

    public static String blue(String text) {
        return enabled ? BLUE + text + RESET : text;
    }

    public static String cyan(String text) {
        return enabled ? CYAN + text + RESET : text;
    }

    public static String gray(String text) {
        return enabled ? GRAY + text + RESET : text;
    }
}
