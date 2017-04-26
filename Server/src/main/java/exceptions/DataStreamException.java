package exceptions;

/**
 * Class of exceptions which are thrown when there is an error with
 * using {@code DataInputStream} or {@code DataOutputStream}.
 */
public class DataStreamException extends Exception {
    public DataStreamException(String message) {
        super(message);
    }
}
