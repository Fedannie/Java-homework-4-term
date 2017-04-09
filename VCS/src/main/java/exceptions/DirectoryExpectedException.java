package exceptions;

/**
 * Trows when trying to init already existing repository.
 */
public class DirectoryExpectedException extends Exception{
    /**
     * Public constructor of DirectoryExpectedException with given message.
     * @param message Message of exception.
     */
    public DirectoryExpectedException(String message) {
        super(message);
    }
}
