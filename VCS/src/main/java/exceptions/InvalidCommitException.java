package exceptions;

/**
 * Throws when commit information is incorrect.
 */
public class InvalidCommitException  extends Exception{
    /**
     * Public constructor of InvalidCommitException with given message.
     * @param message Message of exception.
     */
    public InvalidCommitException (String message) {
        super(message);
    }
}
