package exceptions;

/**
 * Trows when trying to remove branch which was checked out.
 */
public class CanNotDeleteBranchException extends Exception{
    /**
     * Public constructor of CanNotDeleteBranchException with given message.
     * @param message Message of exception.
     */
    public CanNotDeleteBranchException(String message) {
        super(message);
    }
}
