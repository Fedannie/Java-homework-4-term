package exceptions;

/**
 * No such revision exception.
 * Throws when trying to checkout to version which does not exist.
 */
public class NoSuchRevisionException extends Exception{
    /**
     * Public constructor of NoSuchRevisionException with given message.
     * @param message Message of exception.
     */
    public NoSuchRevisionException(String message) {
        super(message);
    }
}