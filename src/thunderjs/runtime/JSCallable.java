package thunderjs.runtime;

import java.util.List;

/**
 * Interface for callable objects in ThunderJS.
 *
 * Implemented by:
 *   - {@link JSFunction} (user-defined functions)
 *   - Built-in functions (Math methods, console.log, etc.)
 */
public interface JSCallable {

    /**
     * Call this function with the given arguments.
     *
     * @param interpreter  the interpreter (for recursive evaluation)
     * @param arguments    the evaluated argument values
     * @return the return value of the function (or JSUndefined if none)
     */
    Object call(Object interpreter, List<Object> arguments);

    /**
     * The number of expected parameters, or -1 for variadic functions.
     * Default is -1 (variadic) so that JSCallable remains a functional interface.
     */
    default int arity() {
        return -1;
    }

    /**
     * Display name for error messages and stack traces.
     */
    default String name() {
        return "<anonymous>";
    }
}
