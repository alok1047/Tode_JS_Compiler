package thunderjs.builtins;

/**
 * Implementation of JavaScript's Math object.
 *
 * All methods are static and return Double values.
 */
public class MathObject {

    public static double floor(double x)  { return Math.floor(x); }
    public static double ceil(double x)   { return Math.ceil(x); }
    public static double round(double x)  { return Math.round(x); }
    public static double abs(double x)    { return Math.abs(x); }
    public static double sqrt(double x)   { return Math.sqrt(x); }
    public static double pow(double x, double y) { return Math.pow(x, y); }
    public static double random()         { return Math.random(); }
    public static double max(double[] args) {
        if (args.length == 0) return Double.NEGATIVE_INFINITY;
        double m = args[0];
        for (int i = 1; i < args.length; i++) {
            if (args[i] > m) m = args[i];
        }
        return m;
    }
    public static double min(double[] args) {
        if (args.length == 0) return Double.POSITIVE_INFINITY;
        double m = args[0];
        for (int i = 1; i < args.length; i++) {
            if (args[i] < m) m = args[i];
        }
        return m;
    }

    // Constants
    public static final double PI = Math.PI;
    public static final double E  = Math.E;
}
