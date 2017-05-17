package reports;

import org.jetbrains.annotations.NotNull;

/**
 * Class which contains information
 * about how did the test passed.
 */
public class Report {
    /**Name of the TestClass test of which are running.*/
    private final @NotNull String className;
    /**Name of the method of TestClass which is running.*/
    private final @NotNull String methodName;

    /**
     * Creates a {@code Report} object by name of the class and name of method in this class.
     * @param className -- name of class test of which are running.
     * @param methodName -- name of method which is running.
     */
    public Report(@NotNull String className, @NotNull String methodName){
        this.className = className;
        this.methodName = methodName;
    }

    /**
     * Main information about execution of a test method.
     * @return full name of method which has been run.
     */
    @NotNull
    public String message() {
        return className + "." + methodName;
    }
}
