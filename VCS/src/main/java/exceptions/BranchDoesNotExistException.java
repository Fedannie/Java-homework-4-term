package exceptions;

/**
 * Trows when trying to remove branch which was not created.
 */
public class BranchDoesNotExistException extends Exception{
    /**
     * Public constructor of BranchDoesNotExistException with given message.
     * @param message Message of exception.
     */
    public BranchDoesNotExistException(String message) {
        super(message);
    }
}
