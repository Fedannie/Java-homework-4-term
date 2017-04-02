package exceptions;

/**
 * Throws when tree file format is incorrect.
 */
public class InvalidTreeException extends Exception{
    /**
     * Public constructor of InvalidTreeException with given message.
     * @param message Message of exception.
     */
    public InvalidTreeException(String message) {
        super(message);
    }
}
