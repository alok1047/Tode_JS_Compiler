package thunderjs.runtime;

import java.util.List;

public class SetConstructor implements JSCallable {
    @Override
    public Object call(Object interpreter, List<Object> arguments) {
        SetObject set = new SetObject();
        if (!arguments.isEmpty() && arguments.get(0) instanceof List<?> list) {
            for (Object item : list) {
                set.getElements().add(item);
            }
        }
        return set;
    }
}
