package reports;

import org.jetbrains.annotations.NotNull;

/**
 * Class which contains information about
 * expected exception which was not got.
 */
public class ExpectedExceptionNotFoundReport extends Report{
    /**
     * Information about expected exception
     * which was not thrown.
     */
    private final @NotNull String exception;
    /**
     * Creates a {@code Report} object by name of the class and name of method in this class.
     *
     * @param className  -- name of class test of which are running.
     * @param methodName -- name of method which is running.
     */
    public ExpectedExceptionNotFoundReport(@NotNull String className, @NotNull String methodName, @NotNull String exceptionName) {
        super(className, methodName);
        exception = exceptionName;
    }

    /**
     * Main information about execution of a test method.
     * @return full name of method which has been run and expected
     * exception which was not thrown.
     */
    @Override
    @NotNull
    public String message(){
        return super.message() + "\n\tExpected exception which was not thrown: " + exception;
    }
}
