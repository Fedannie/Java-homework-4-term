package exceptions;

/**
 * No such command exception.
 * Throws by command line arguments handler when command is unknown or number of arguments is incorrect.
 */
public class NoSuchCommandException extends Exception{
    /**
     * Public constructor of NoSuchCommandException with given message.
     * @param message Message of exception.
     */
    public NoSuchCommandException(String message) {
        super(message);
    }
}
