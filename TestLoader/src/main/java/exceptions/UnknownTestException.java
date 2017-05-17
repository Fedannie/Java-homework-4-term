package exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown when there are any problems with the structure of tested class.
 */
public class UnknownTestException extends Exception {
    /**
     * Public constructor of {@code UnknownTestException} object by a string message.
     * @param message -- keeps information about caused problem.
     */
    public UnknownTestException (@NotNull String message) {
        super(message);
    }
}
