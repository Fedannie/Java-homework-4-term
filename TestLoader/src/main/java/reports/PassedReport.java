package reports;

import org.jetbrains.annotations.NotNull;

/**
 * Class of report of the test which has passed correctly.
 */
public class PassedReport extends Report{
    /**
     * Time in milliseconds.
     * Shows for how long had test method been executed.
     */
    private final @NotNull long time;
    /**
     * Creates a {@code PassedReport} object by name of the class and name of method in this class.
     *
     * @param className  -- name of class test of which are running.
     * @param methodName -- name of method which is running.
     */
    public PassedReport(@NotNull String className, @NotNull String methodName, @NotNull long time) {
        super(className, methodName);
        this.time = time;
    }

    /**
     * Main information about execution of a test method.
     * @return full name of method which has been run and time of its execution in milliseconds.
     */
    @Override
    @NotNull
    public String message(){
        return super.message() + "\n\tTest has passed in " + time + " milliseconds.";
    }
}
