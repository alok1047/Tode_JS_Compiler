package thunderjs.util;

import thunderjs.runtime.JSCallable;
import thunderjs.runtime.JSFunction;
import thunderjs.runtime.JSNull;
import thunderjs.runtime.JSUndefined;
import thunderjs.runtime.DateObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaScript-style value → string conversion.
 *
 * Ensures console.log output matches Node.js behavior:
 *   - Arrays print as "1,2,3" (no spaces, no brackets) when toString()
 *   - Arrays print as "[ 1, 2, 3 ]" when inspected
 *   - Objects print as "{ key: value }"
 *   - null → "null", undefined → "undefined"
 *   - Numbers: integers print without ".0"
 */
public class Stringify {

    /**
     * Convert a JS value to its string representation for console.log.
     * Matches Node.js console.log behavior.
     */
    @SuppressWarnings("unchecked")
    public static String stringify(Object value) {
        if (value == null || value instanceof JSUndefined) return "undefined";
        if (value instanceof JSNull) return "null";

        if (value instanceof Double d) {
            // Print integers without .0
            if (d == Math.floor(d) && !Double.isInfinite(d) && !Double.isNaN(d)) {
                long l = d.longValue();
                return Long.toString(l);
            }
            if (Double.isNaN(d)) return "NaN";
            if (Double.isInfinite(d)) return d > 0 ? "Infinity" : "-Infinity";
            return Double.toString(d);
        }

        if (value instanceof Boolean) return value.toString();
        if (value instanceof String) return (String) value;

        if (value instanceof ArrayList<?> arr) {
            return stringifyArray((List<Object>) arr);
        }

        if (value instanceof LinkedHashMap<?, ?> map) {
            return stringifyObject((Map<String, Object>) map);
        }

        if (value instanceof JSFunction fn) {
            return fn.toString();
        }

        if (value instanceof JSCallable) {
            return "function () { [native code] }";
        }

        return value.toString();
    }

    /**
     * Stringify a value for use inside console.log output.
     * Strings inside arrays/objects get quoted.
     */
    @SuppressWarnings("unchecked")
    public static String inspect(Object value) {
        if (value instanceof String s) return "'" + s + "'";
        if (value instanceof ArrayList<?> arr) {
            return inspectArray((List<Object>) arr);
        }
        if (value instanceof LinkedHashMap<?, ?> map) {
            return stringifyObject((Map<String, Object>) map);
        }
        return stringify(value);
    }

    /**
     * Array.toString() behavior: elements separated by comma, no brackets.
     * e.g., [1, 2, 3].toString() → "1,2,3"
     */
    private static String stringifyArray(List<Object> arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.size(); i++) {
            if (i > 0) sb.append(",");
            Object elem = arr.get(i);
            if (elem instanceof JSNull || elem instanceof JSUndefined || elem == null) {
                sb.append("");
            } else {
                sb.append(stringify(elem));
            }
        }
        return sb.toString();
    }

    /**
     * Array inspection for console.log: [ 1, 2, 3 ]
     */
    public static String inspectArray(List<Object> arr) {
        if (arr.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[ ");
        for (int i = 0; i < arr.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(inspect(arr.get(i)));
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * Object stringification: { key: value, key2: value2 }
     */
    private static String stringifyObject(Map<String, Object> map) {
        if (map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{ ");
        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (i > 0) sb.append(", ");
            sb.append(entry.getKey()).append(": ").append(inspect(entry.getValue()));
            i++;
        }
        sb.append(" }");
        return sb.toString();
    }

    /**
     * Convert a value to JS string (for String() coercion and + operator).
     */
    public static String toJSString(Object value) {
        return stringify(value);
    }

    /**
     * Convert a value to JS number (for Number() coercion).
     */
    public static double toJSNumber(Object value) {
        if (value == null || value instanceof JSUndefined) return Double.NaN;
        if (value instanceof JSNull) return 0.0;
        if (value instanceof Double d) return d;
        if (value instanceof DateObject d) return d.getJSDate().getTime();
        if (value instanceof Boolean b) return b ? 1.0 : 0.0;
        if (value instanceof String s) {
            s = s.trim();
            if (s.isEmpty()) return 0.0;
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }

    /**
     * Convert a value to JS boolean (for Boolean() coercion).
     */
    public static boolean toJSBoolean(Object value) {
        return isTruthy(value);
    }

    /**
     * JavaScript truthiness rules.
     * Falsy: 0, "", null, undefined, NaN, false
     * Truthy: everything else (including [] and {})
     */
    public static boolean isTruthy(Object value) {
        if (value == null || value instanceof JSUndefined || value instanceof JSNull) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Double d) {
            return d != 0.0 && !Double.isNaN(d);
        }
        if (value instanceof String s) return !s.isEmpty();
        // Arrays and objects are always truthy
        return true;
    }
}
