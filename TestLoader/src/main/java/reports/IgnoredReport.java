package reports;

import org.jetbrains.annotations.NotNull;

/**Class which contains information about ignored test method.*/
public class IgnoredReport extends Report{
    /**Reason why current test was ignored.*/
    private final @NotNull String reason;
    /**
     * Creates a {@code Report} object by name of the class and name of method in this class.
     *
     * @param className  -- name of class test of which are running.
     * @param methodName -- name of method which is running.
     */
    public IgnoredReport(@NotNull String className, @NotNull String methodName, @NotNull String reason) {
        super(className, methodName);
        this.reason = reason;
    }

    /**
     * Main information about execution of a test method.
     * @return full name of method which has been run and
     * cause why method was ignored.
     */
    @Override
    @NotNull
    public String message(){
        return super.message() + "\nTest was ignored because of: " + reason;
    }
}
