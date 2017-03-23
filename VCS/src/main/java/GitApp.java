import exceptions.NoSuchCommandException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides console access to MyGit system.
 */
public class GitApp {
    /**
     * Parses and execute MyGit commands. Commands and parameters are given as arguments of command line.
     * @param args MyGit commands and their parameters. Given as command line parameters.
     */
    public static void main(String[] args) {
        final Path currentDirectory = Paths.get(System.getProperty("user.dir"));
        final ArgsHandler handler = new ArgsHandler(currentDirectory);
        try {
            handler.handle(args);
        } catch (NoSuchCommandException e) {
            System.out.println("This command is not allowed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Operation failed: " + e.getClass().toString() + e.getMessage());
        }
    }
}
