package thunderjs.runtime;

import java.util.List;

public class MapConstructor implements JSCallable {
    @Override
    public Object call(Object interpreter, List<Object> arguments) {
        MapObject mapObj = new MapObject();
        if (!arguments.isEmpty() && arguments.get(0) instanceof List<?> list) {
            for (Object entry : list) {
                if (entry instanceof List<?> pair && pair.size() >= 2) {
                    mapObj.getMap().put(pair.get(0), pair.get(1));
                }
            }
        }
        return mapObj;
    }
}
