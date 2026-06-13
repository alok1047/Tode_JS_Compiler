package thunderjs.runtime;

import java.util.List;
import thunderjs.interpreter.Interpreter;
import thunderjs.util.Stringify;

public class DateConstructor implements JSCallable {

    @Override
    public Object call(Object interpreter, List<Object> arguments) {
        // Date() called as a function returns a string representing the current date/time
        return new JSDate().toString();
    }

    public Object getProperty(String name) {
        return switch (name) {
            case "now" -> (JSCallable) (interpreter, args) -> JSDate.now();
            case "parse" -> (JSCallable) (interpreter, args) -> {
                String str = args.isEmpty() ? "" : Stringify.toJSString(args.get(0));
                return JSDate.parse(str);
            };
            case "UTC" -> (JSCallable) (interpreter, args) -> {
                double y = args.size() > 0 ? Stringify.toJSNumber(args.get(0)) : Double.NaN;
                double m = args.size() > 1 ? Stringify.toJSNumber(args.get(1)) : 0;
                double d = args.size() > 2 ? Stringify.toJSNumber(args.get(2)) : 1;
                double hh = args.size() > 3 ? Stringify.toJSNumber(args.get(3)) : 0;
                double mm = args.size() > 4 ? Stringify.toJSNumber(args.get(4)) : 0;
                double ss = args.size() > 5 ? Stringify.toJSNumber(args.get(5)) : 0;
                double ms = args.size() > 6 ? Stringify.toJSNumber(args.get(6)) : 0;
                return JSDate.UTC(y, m, d, hh, mm, ss, ms);
            };
            default -> JSUndefined.INSTANCE;
        };
    }
}
