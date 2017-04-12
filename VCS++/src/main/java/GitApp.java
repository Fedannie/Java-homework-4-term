import exceptions.NoSuchCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides console access to MyGit system.
 */
public class GitApp {
    /**Keeps some information about work process.*/
    private static final Logger logger = LoggerFactory.getLogger(GitApp.class);

    /**
     * Parses and execute MyGit commands. Commands and parameters are given as arguments of command line.
     * @param args MyGit commands and their parameters. Given as command line parameters.
     */
    public static void main(String[] args) {
        final Path currentDirectory = Paths.get(System.getProperty("user.dir"));
        final ArgsHandler handler = new ArgsHandler(currentDirectory);
        try {
            logger.info("Working started with arguments: '{}'", (Object) args);
            handler.handle(args);
        } catch (NoSuchCommandException e) {
            String message = "This command is not allowed: " + e.getMessage();
            logger.error(message);
            System.out.println(message);
        } catch (Exception e) {
            String message = "Operation failed: " + e.getClass().toString() + e.getMessage();
            logger.error(message);
            System.out.println(message);
        }
    }
}
