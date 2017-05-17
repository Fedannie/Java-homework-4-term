import org.jetbrains.annotations.NotNull;
import reports.Report;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class which provides work of MyJUnit
 * with interface of a command-line application.
 */
public class TesterMain {
    /**Main method. Handles arguments of command line and perform execution of testing process.*/
    public static void main(String[] args) {
        if (args.length != 1){
            printError("One argument expected. Please, try again.");
            System.exit(0);
        }

        Path pathToClass = Paths.get(args[0]);
        if (pathToClass == null){
            printError("Given path does not exist. Please, try again.");
            System.exit(0);
        }

        try {
            ClassPicker classesCollector = new ClassPicker(pathToClass);
            Files.walkFileTree(pathToClass, classesCollector);

            for (Class<?> testClass : classesCollector.getTestClasses()) {
                try {
                    for (Report report : new Runner(testClass).test()) {
                        System.out.println(report.message());
                    }
                } catch (Exception e){
                    printError(e.getClass().getName() + " thrown with cause: " + e.getCause() +
                            " with message: " + e.getMessage());
                }
            }
        } catch (Throwable e){
            printError(e.getClass().getName() + " thrown with message: " + e.getMessage() + ".");
            System.exit(0);
        }
    }

    /**
     * Print error to console.
     * @param message -- given error.
     */
    private static void printError(@NotNull String message){
        System.out.println(message);
    }
}
