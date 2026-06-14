package thunderjs.runtime;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SetObject {
    private final Set<Object> elements = new LinkedHashSet<>();

    public Set<Object> getElements() {
        return elements;
    }

    public Object getProperty(String name, int line) {
        return switch (name) {
            case "size" -> (double) elements.size();
            case "add" -> (JSCallable) (interpreter, args) -> {
                if (!args.isEmpty()) {
                    elements.add(args.get(0));
                }
                return this;
            };
            case "delete" -> (JSCallable) (interpreter, args) -> {
                if (args.isEmpty()) return false;
                return elements.remove(args.get(0));
            };
            case "has" -> (JSCallable) (interpreter, args) -> {
                if (args.isEmpty()) return false;
                return elements.contains(args.get(0));
            };
            case "clear" -> (JSCallable) (interpreter, args) -> {
                elements.clear();
                return JSUndefined.INSTANCE;
            };
            default -> JSUndefined.INSTANCE;
        };
    }
}
