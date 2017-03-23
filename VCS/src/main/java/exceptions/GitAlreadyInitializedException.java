package exceptions;

/**
 * Trows when trying to init already existing repository.
 */
public class GitAlreadyInitializedException extends Exception{
    /**
     * Public constructor of GitAlreadyInitializedException with given message.
     * @param message Message of exception.
     */
    public GitAlreadyInitializedException(String message) {
        super(message);
    }
}
