package exceptions;

/**
 * Throws when tying to make checkout with some changes in repository made after last commit.
 */
public class IndexFileNotEmptyException extends Exception{
    /**
     * Public constructor of IndexFileNotEmptyException with given message.
     * @param message Message of exception.
     */
    public IndexFileNotEmptyException (String message) {
        super(message);
    }
}