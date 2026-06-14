package thunderjs.runtime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapObject {
    private final Map<Object, Object> map = new LinkedHashMap<>();

    public Map<Object, Object> getMap() {
        return map;
    }

    public Object getProperty(String name, int line) {
        return switch (name) {
            case "size" -> (double) map.size();
            case "set" -> (JSCallable) (interpreter, args) -> {
                if (args.size() >= 2) {
                    map.put(args.get(0), args.get(1));
                } else if (args.size() == 1) {
                    map.put(args.get(0), JSUndefined.INSTANCE);
                }
                return this;
            };
            case "get" -> (JSCallable) (interpreter, args) -> {
                if (args.isEmpty()) return JSUndefined.INSTANCE;
                Object val = map.get(args.get(0));
                return val != null ? val : JSUndefined.INSTANCE;
            };
            case "delete" -> (JSCallable) (interpreter, args) -> {
                if (args.isEmpty()) return false;
                return map.remove(args.get(0)) != null;
            };
            case "has" -> (JSCallable) (interpreter, args) -> {
                if (args.isEmpty()) return false;
                return map.containsKey(args.get(0));
            };
            case "clear" -> (JSCallable) (interpreter, args) -> {
                map.clear();
                return JSUndefined.INSTANCE;
            };
            default -> JSUndefined.INSTANCE;
        };
    }
}
