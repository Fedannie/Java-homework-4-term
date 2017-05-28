package exceptions;

/**Thrown when there is a problem with writing file to index file.*/
public class AddFileToIndexFailed extends Exception{
    /**
     * Public constructor of AddFileToIndexFailed with given message.
     * @param message Message of exception.
     */
    public AddFileToIndexFailed(String message) {
        super(message);
    }
}
