package thunderjs.runtime;

/**
 * Singleton representing JavaScript's {@code null} value.
 *
 * Using a dedicated type (rather than Java null) lets us distinguish
 * between "variable is null" and "variable is undefined" in the
 * runtime environment.
 */
public final class JSNull {

    /** The single instance of JS null. */
    public static final JSNull INSTANCE = new JSNull();

    private JSNull() {}

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JSNull;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
