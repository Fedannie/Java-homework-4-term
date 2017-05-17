package reports;

import org.jetbrains.annotations.NotNull;

/**
 * Class which contains information about
 * unexpected exception with which current
 * test method has failed.
 */
public class UnexpectedExceptionReport extends Report{
    /**
     * Information about unexpected exception
     * thrown in test method.
     */
    private final @NotNull String exception;
    /**
     * Creates a {@code Report} object by name of the class and name of method in this class.
     *
     * @param className  -- name of class test of which are running.
     * @param methodName -- name of method which is running.
     */
    public UnexpectedExceptionReport(@NotNull String className, @NotNull String methodName, @NotNull Throwable e) {
        super(className, methodName);
        exception = "Unexpected exception: " + e.getClass().getName() +
                ", with message: " + e.getMessage();
    }

    /**
     * Main information about execution of a test method.
     * @return full name of method which has been run and unexpected
     * exception which was thrown.
     */
    @Override
    @NotNull public String message(){
        return super.message() + "\n\t" + exception;
    }
}
