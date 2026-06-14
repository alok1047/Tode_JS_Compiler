package thunderjs.util;

import thunderjs.runtime.Environment;
import java.util.Collection;
import java.util.List;

public class SuggestionEngine {
    
    private static final List<String> BUILTINS = List.of(
        "console", "Math", "Object", "Date", "Array", "String", "Number", "Boolean",
        "parseInt", "parseFloat", "isNaN", "isFinite", "NaN", "Infinity", "undefined"
    );

    public static String suggestVariable(String name, Environment env) {
        if (name == null) return null;
        java.util.Set<String> candidates = new java.util.HashSet<>(env.getAllVisibleNames());
        candidates.addAll(BUILTINS);
        return Levenshtein.closest(name, candidates);
    }

    public static String suggestProperty(String name, Collection<String> properties) {
        if (name == null || properties == null) return null;
        return Levenshtein.closest(name, properties);
    }

    public static String suggestBuiltin(String name) {
        if (name == null) return null;
        return Levenshtein.closest(name, BUILTINS);
    }
}
