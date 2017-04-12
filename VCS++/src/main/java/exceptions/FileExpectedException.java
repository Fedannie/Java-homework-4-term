package exceptions;

/**
 * Trows when trying to init already existing repository.
 */
public class FileExpectedException extends Exception{
    /**
     * Public constructor of FileExpectedException with given message.
     * @param message Message of exception.
     */
    public FileExpectedException(String message) {
        super(message);
    }
}