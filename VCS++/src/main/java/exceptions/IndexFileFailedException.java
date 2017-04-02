package exceptions;

/**
 * Throws when index file contains bad information.
 */
public class IndexFileFailedException extends Exception{
    /**
     * Public constructor of IndexFileFailedException with given message.
     * @param message Message of exception.
     */
    public IndexFileFailedException(String message) {
        super(message);
    }
}
