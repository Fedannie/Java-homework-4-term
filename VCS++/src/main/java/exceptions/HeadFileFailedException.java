package exceptions;

/**
 * Throws when index file contains bad information.
 */
public class HeadFileFailedException extends Exception{
    /**
     * Public constructor of HeadFileFailedException with given message.
     * @param message Message of exception.
     */
    public HeadFileFailedException(String message) {
        super(message);
    }
}
