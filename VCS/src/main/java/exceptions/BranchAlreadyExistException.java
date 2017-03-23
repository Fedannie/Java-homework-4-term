package exceptions;

/**
 * Trows when trying to create already existing branch.
 */
public class BranchAlreadyExistException extends Exception{
    /**
     * Public constructor of BranchAlreadyExistException with given message.
     * @param message Message of exception.
     */
    public BranchAlreadyExistException(String message) {
        super(message);
    }
}
