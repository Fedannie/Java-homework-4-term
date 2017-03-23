import com.sun.istack.internal.NotNull;
import exceptions.*;
import repository.Manager;
import repository.Repository;

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

    /** Current directory. */
    @NotNull
    private final Path currentDirectory;

    /**
     * Public constructor with one parameter.
     *
     * @param path Current directory.
     */
    ArgsHandler(@NotNull Path path) {
        currentDirectory = path;
    }

    /**
     * Parse and handle entered command from command line.
     * @param args Entered arguments from command line.
     * @throws NoSuchCommandException Thrown when command is unknown or number of arguments is incorrect.
     */
    void handle(String[] args) throws
            NoSuchCommandException, GitAlreadyInitializedException, DirectoryExpectedException, IOException,
            BranchAlreadyExistException, HeadFileFailedException, GitNotInitializedException, IndexFileFailedException,
            CanNotDeleteBranchException, BranchDoesNotExistException, InvalidTreeException, InvalidCommitException,
            IndexFileNotEmptyException, NoSuchRevisionException{
        if (args.length == 0) {
            throw new NoSuchCommandException("No arguments");
        }
        if (args[0].equals(HELP_CMD)){
            printHelp();
        }
        if (args[0].equals(INIT_CMD)) {
            Manager.init(currentDirectory);
            System.out.println("New repository successfully initialized.");
            return;
        }
        Path path = Paths.get("").toAbsolutePath();
        Repository repository = Manager.findRepository(path);
        switch (args[0]) {
            case ADD_CMD:
                if (args.length > 1) {
                    for (Path new_path : suffixArgsToList(args)) {
                        Manager.addToIndex(new_path);
                    }
                    break;
                }
                throw new NoSuchCommandException(ADD_CMD + " requires some files to have an effect");
            case REMOVE_CMD:
                if (args.length > 1) {
                    for (Path new_path : suffixArgsToList(args)) {
                        Manager.removeFromIndex(new_path);
                    }
                    break;
                }
                throw new NoSuchCommandException(REMOVE_CMD + " requires some files to have an effect");
            case BRANCH_CMD:
                if (args.length == 1) {
                    Manager.allBranches(repository);
                    break;
                }
                if (args.length == 2) {
                    Manager.newBranch(repository, args[1]);
                    break;
                }
                if (args.length == 3 && args[1].equals("-d")) {
                    Manager.deleteBranch(repository, args[2]);
                    return;
                }
                throw new NoSuchCommandException(BRANCH_CMD + " entered wrong number of arguments");
            case CHECKOUT_CMD:
                if (args.length > 1) {
                    Manager.checkout(repository, args[1]);
                    return;
                }
                throw new NoSuchCommandException(CHECKOUT_CMD + " requires a revision name");
            case COMMIT_CMD:
                if (args.length > 1) {
                    Manager.commit(repository, args[1], null);
                    break;
                }
                throw new NoSuchCommandException(COMMIT_CMD + " requires a message");
            case MERGE_CMD:
                if (args.length > 1) {
                    Manager.merge(repository, args[1]);
                    break;
                }
                throw new NoSuchCommandException(MERGE_CMD + " requires another branch");
            default:
                break;
        }
    }

    /**Prints help to console.*/
    private static void printHelp() {
        System.out.println("usage:" + "\n\t" +
                "init [<path>]" + "\n\t" +
                "add <path>" + "\n\t" +
                "remove <path>" + "\n\t" +
                "commit <message>" + "\n\t" +
                "branch <name>" + "\n\t" +
                "branch -d <name>" + "\n\t" +
                "merge <name>" + "\n\t" +
                "help");
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
