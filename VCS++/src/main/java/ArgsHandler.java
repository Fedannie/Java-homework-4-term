
import exceptions.*;
import git_objects.Log;
import git_objects.StatusEntry;
import org.jetbrains.annotations.NotNull;
import repository.Manager;
import repository.StatusManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Parses and handles arguments of console app. */
class ArgsHandler {
    /** Allowable arguments of command line.*/
    private static final String ADD_CMD = "add";
    private static final String BRANCH_CMD = "branch";
    private static final String CHECKOUT_CMD = "checkout";
    private static final String COMMIT_CMD = "commit";
    private static final String HELP_CMD = "help";
    private static final String INIT_CMD = "init";
    private static final String MERGE_CMD = "merge";
    private static final String REMOVE_CMD = "remove";
    private static final String LOG_CMD = "log";
    private static final String CLEAN_CMD = "clean";
    private static final String STATUS_CMD = "status";
    private static final String RESET_CMD = "reset";

    /** Current directory. */
    @NotNull
    private final Path currentDirectory;

    /**
     * Constructor with one parameter. Creates new {@code ArgsHandler} object by given path.
     * @param path Current directory.
     */
    ArgsHandler(@NotNull Path path) {
        currentDirectory = path;
    }

    /**
     * Parse and handle entered command from command line.
     * @param args Entered arguments from command line.
     * @throws NoSuchCommandException Thrown when command is unknown or number of arguments is incorrect.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws NoSuchRevisionException Throws when trying to checkout to version which does not exist.
     * @throws IndexFileFailedException Throws when reading of index file failed.
     * @throws InvalidTreeException Throws when tree file format is incorrect.
     * @throws GitAlreadyInitializedException Throws when trying to init new repository which already exists.
     * @throws IOException Throws when something is wrong with file system.
     * @throws BranchDoesNotExistException Throws when trying to delete branch which does not exist.
     * @throws CanNotDeleteBranchException Throws when trying to delete branch which HEAD file points on.
     * @throws BranchAlreadyExistException Throws when trying to create branch that already exists.
     * @throws GitNotInitializedException Throws when trying to work with repository that was not inited.
     */
    void handle(String[] args) throws
            NoSuchCommandException, GitAlreadyInitializedException, DirectoryExpectedException, IOException,
            BranchAlreadyExistException, HeadFileFailedException, GitNotInitializedException, IndexFileFailedException,
            CanNotDeleteBranchException, BranchDoesNotExistException, InvalidTreeException, InvalidCommitException,
            NoSuchRevisionException{
        if (args.length == 0) {
            throw new NoSuchCommandException("No arguments");
        }
        if (args[0].equals(HELP_CMD)){
            printHelp();
            return;
        }
        if (args[0].equals(INIT_CMD)) {
            Manager.init(currentDirectory);
            System.out.println("New repository successfully initialized.");
            return;
        }
        Path path = Paths.get("").toAbsolutePath();
        Manager manager = new Manager(path);
        switch (args[0]) {
            case ADD_CMD:
                if (args.length > 1) {
                    for (Path new_path : suffixArgsToList(args)) {
                        manager.addToIndex(new_path);
                    }
                    break;
                }
                throw new NoSuchCommandException(ADD_CMD + " requires some files to have an effect");
            case REMOVE_CMD:
                if (args.length > 1) {
                    for (Path new_path : suffixArgsToList(args)) {
                        manager.removeFromIndex(new_path);
                    }
                    break;
                }
                throw new NoSuchCommandException(REMOVE_CMD + " requires some files to have an effect");
            case BRANCH_CMD:
                if (args.length == 1) {
                    manager.allBranches();
                    break;
                }
                if (args.length == 2) {
                    manager.newBranch(args[1]);
                    break;
                }
                if (args.length == 3 && args[1].equals("-d")) {
                    manager.deleteBranch(args[2]);
                    return;
                }
                throw new NoSuchCommandException(BRANCH_CMD + " entered wrong number of arguments");
            case CHECKOUT_CMD:
                if (args.length > 1) {
                    manager.checkout(args[1]);
                    return;
                }
                throw new NoSuchCommandException(CHECKOUT_CMD + " requires a revision name");
            case COMMIT_CMD:
                if (args.length > 1) {
                    manager.commit(args[1], null);
                    break;
                }
                throw new NoSuchCommandException(COMMIT_CMD + " requires a message");
            case MERGE_CMD:
                if (args.length > 1) {
                    manager.merge(args[1]);
                    break;
                }
                throw new NoSuchCommandException(MERGE_CMD + " requires a branch");
            case LOG_CMD:
                Log currentLog = manager.getLog();
                while (currentLog != null) {
                    System.out.println(currentLog.getMessage());
                    currentLog = currentLog.getNextLogMessage();
                }
                break;
            case CLEAN_CMD:
                manager.cleanRepository();
                break;
            case RESET_CMD:
                if (args.length > 1) {
                    manager.resetFile(Paths.get(args[1]));
                    break;
                }
                throw new NoSuchCommandException(RESET_CMD + " requires path to file");
            case STATUS_CMD:
                StatusManager statusManager = manager.getRepositoryStatus();
                System.out.println("Revision: " + statusManager.getRevision());
                for (StatusEntry entry : statusManager.getEntries()) {
                    System.out.println("\t" + entry.getType() + "\t" + entry.getPath());
                }
                break;
            default:
                break;
        }
    }

    /**Prints help to console.*/
    private static void printHelp() {
        System.out.println("usage:" + "\n\t" +
                INIT_CMD + " <path>" + "\n\t" +
                ADD_CMD + " <path>" + "\n\t" +
                REMOVE_CMD + " <path>" + "\n\t" +
                COMMIT_CMD + " <message>" + "\n\t" +
                BRANCH_CMD + " <name>" + "\n\t" +
                BRANCH_CMD + " -d <name>" + "\n\t" +
                MERGE_CMD + " <name>" + "\n\t" +
                LOG_CMD + "\n\t" +
                STATUS_CMD + "\n\t" +
                RESET_CMD + " <path>" + "\n\t" +
                CLEAN_CMD + "\n\t" +
                HELP_CMD);
    }

    /**
     * Converts array of strings to list of paths.
     * @param args Strings wanted to be converted to paths.
     * @return List of paths.
     */
    @NotNull
    private static List<Path> suffixArgsToList(@NotNull String[] args) {
        Path path = Paths.get("").toAbsolutePath();
        List<Object> answer = Arrays.asList(Arrays.stream(args).map(arg -> path.resolve(arg).normalize()).toArray()).subList(1, args.length);
        return answer.stream().map(e -> (Path) e).collect(Collectors.toList());
    }
}
