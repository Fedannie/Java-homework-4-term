package exceptions;

/**
 * Throws when trying to work with repository which does not exist.
 */
public class GitNotInitializedException extends Exception{
    /**
     * Public constructor of GitNotInitializedException with given message.
     * @param message Message of exception.
     */
    public GitNotInitializedException(String message) {
        super(message);
    }
}
