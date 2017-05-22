package exceptions;

/**
 * Class of exceptions which are thrown when an error appears
 * while working with {@code Socket}.
 */
public class SocketException extends Exception {
    public SocketException(String message) {
        super(message);
    }
}
