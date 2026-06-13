package thunderjs.runtime;

/**
 * Singleton representing JavaScript's {@code undefined} value.
 */
public final class JSUndefined {

    /** The single instance of JS undefined. */
    public static final JSUndefined INSTANCE = new JSUndefined();

    private JSUndefined() {}

    @Override
    public String toString() {
        return "undefined";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JSUndefined;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
