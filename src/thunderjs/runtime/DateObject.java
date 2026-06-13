package thunderjs.runtime;

import java.util.List;
import thunderjs.util.Stringify;

public class DateObject {
    private final JSDate date;

    public DateObject(JSDate date) {
        this.date = date;
    }

    public JSDate getJSDate() {
        return date;
    }

    @Override
    public String toString() {
        return date.toString();
    }

    public Object getProperty(String name, int line) {
        return switch (name) {
            // Getters
            case "getFullYear" -> (JSCallable) (interpreter, args) -> date.getFullYear();
            case "getMonth" -> (JSCallable) (interpreter, args) -> date.getMonth();
            case "getDate" -> (JSCallable) (interpreter, args) -> date.getDate();
            case "getDay" -> (JSCallable) (interpreter, args) -> date.getDay();
            case "getHours" -> (JSCallable) (interpreter, args) -> date.getHours();
            case "getMinutes" -> (JSCallable) (interpreter, args) -> date.getMinutes();
            case "getSeconds" -> (JSCallable) (interpreter, args) -> date.getSeconds();
            case "getMilliseconds" -> (JSCallable) (interpreter, args) -> date.getMilliseconds();
            case "getTime" -> (JSCallable) (interpreter, args) -> date.getTime();
            case "getTimezoneOffset" -> (JSCallable) (interpreter, args) -> date.getTimezoneOffset();

            // Setters
            case "setFullYear" -> (JSCallable) (interpreter, args) -> {
                double val = args.isEmpty() ? Double.NaN : Stringify.toJSNumber(args.get(0));
                return date.setFullYear(val);
            };
            case "setMonth" -> (JSCallable) (interpreter, args) -> {
                double val = args.isEmpty() ? Double.NaN : Stringify.toJSNumber(args.get(0));
                return date.setMonth(val);
            };
            case "setDate" -> (JSCallable) (interpreter, args) -> {
                double val = args.isEmpty() ? Double.NaN : Stringify.toJSNumber(args.get(0));
                return date.setDate(val);
            };
            case "setHours" -> (JSCallable) (interpreter, args) -> {
                double val = args.isEmpty() ? Double.NaN : Stringify.toJSNumber(args.get(0));
                return date.setHours(val);
            };
            case "setMinutes" -> (JSCallable) (interpreter, args) -> {
                double val = args.isEmpty() ? Double.NaN : Stringify.toJSNumber(args.get(0));
                return date.setMinutes(val);
            };
            case "setSeconds" -> (JSCallable) (interpreter, args) -> {
                double val = args.isEmpty() ? Double.NaN : Stringify.toJSNumber(args.get(0));
                return date.setSeconds(val);
            };
            case "setMilliseconds" -> (JSCallable) (interpreter, args) -> {
                double val = args.isEmpty() ? Double.NaN : Stringify.toJSNumber(args.get(0));
                return date.setMilliseconds(val);
            };
            case "setTime" -> (JSCallable) (interpreter, args) -> {
                double val = args.isEmpty() ? Double.NaN : Stringify.toJSNumber(args.get(0));
                return date.setTime(val);
            };

            // String conversions
            case "toString" -> (JSCallable) (interpreter, args) -> date.toString();
            case "toISOString" -> (JSCallable) (interpreter, args) -> {
                try {
                    return date.toISOString();
                } catch (IllegalArgumentException e) {
                    throw new RuntimeError("RangeError: Invalid time value", line);
                }
            };
            case "toUTCString" -> (JSCallable) (interpreter, args) -> date.toUTCString();
            case "toDateString" -> (JSCallable) (interpreter, args) -> date.toDateString();
            case "toTimeString" -> (JSCallable) (interpreter, args) -> date.toTimeString();
            case "toJSON" -> (JSCallable) (interpreter, args) -> date.toJSON();
            
            // ValueOf coercion (often called under the hood in JS relational operations)
            case "valueOf" -> (JSCallable) (interpreter, args) -> date.getTime();

            default -> JSUndefined.INSTANCE;
        };
    }
}
