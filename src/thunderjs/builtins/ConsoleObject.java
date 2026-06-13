package thunderjs.builtins;

import thunderjs.util.Stringify;
import java.util.List;

/**
 * Implementation of JavaScript's console object.
 *
 * Currently supports:
 *   - console.log(arg1, arg2, ...) — prints arguments separated by spaces
 */
public class ConsoleObject {

    public interface Listener {
        void onLog(String text);
    }

    public static Listener listener = null;

    /**
     * console.log implementation.
     * Prints all arguments separated by spaces, followed by a newline.
     * Matches Node.js output format.
     */
    public static void log(List<Object> args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(Stringify.stringify(args.get(i)));
        }
        String text = sb.toString();
        if (listener != null) {
            listener.onLog(text);
        } else {
            System.out.println(text);
        }
    }
}
