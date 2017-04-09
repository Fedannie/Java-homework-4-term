package repository;

import com.google.common.base.Splitter;
import exceptions.*;
import git_objects.Commit;
import git_objects.GitObject;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class which refers to MyGit repository.
 */
public class Repository {
    /**Name of folder in which every repository should be located.*/
    private static final String REPOSITORY_DIRECTORY_NAME = ".vcs";
    /**Name of folder {@code objects} in MyGit repository.*/
    private static final String OBJECTS_FOLDER_NAME = "objects";
    /**Name of folder {@code references} in MyGit repository.*/
    private static final String REFERENCES_FOLDER_NAME = "refs";
    /**Name of {@code HEAD} file in MyGit repository.*/
    private static final String HEAD_FILE_NAME = "HEAD";
    /**Name of {@code index} file in MyGit repository.*/
    private static final String INDEX_FILE_NAME = "index";
    /**Name of default branch name in MyGit repository.*/
    public static final String DEFAULT_BRANCH_NAME = "master";
    /**Name of username property in MyGit repository.*/
    static final String USER_NAME_PROPERTY = "user.name";
    /**Length of name of {@code objects} folder in MyGit repository.*/
    private static final int OBJECTS_FOLDER_NAME_LENGTH = 2;
    /**Prefix that is added to HEAD reference.*/
    static final String REFERENCE_HEAD_PREFIX = "ref: ";
    /**Prefix that is added to name of parent of commit.*/
    public static final String PARENT_COMMIT_PREFIX = "parent: ";
    /**Prefix that is added to commit message.*/
    public static final String MESSAGE_COMMIT_PREFIX = "message: ";
    /**Prefix tha is added to commit message if it was merged.*/
    static final String MERGE_COMMIT_PREFIX = "merge: ";

    /**Directory of this repository.*/
    private final @NotNull Path directory;
    /**Path of HEAD file of this repository.*/
    private final @NotNull Path HEAD;
    /**Path of index file of this repository.*/
    private final @NotNull Path index;
    /**Path of objects directory of this repository.*/
    private final @NotNull Path objects;
    /**Path of references directory of this repository.*/
    private final @NotNull Path references;

    /**
     * Get Path to folder in which repository is located.
     * @return Path as Path class object.
     */
    @NotNull Path getDirectory() {
        return directory;
    }

    /**
     * Get Path to HEAD file.
     * @return Path as Path class object.
     */
    @NotNull Path getHEAD() {
        return HEAD;
    }

    /**
     * Get Path to folder objects in repository.
     * @return Path as Path class object.
     */
    @NotNull
    private Path getObjects() {
        return objects;
    }

    /**
     * Get Path to folder references in repository.
     * @return Path as Path class object.
     */
    @NotNull Path getReferences() {
        return references;
    }

    /**
     * Private constructor of repository with all its inwards..
     * @param directory Where new repository will be inited.
     * @throws IOException Throws when has any problems with file system.
     */
    private Repository(@NotNull Path directory) throws IOException{
        this.directory = directory;
        HEAD = directory.resolve(HEAD_FILE_NAME);
        index = directory.resolve(INDEX_FILE_NAME);
        objects = directory.resolve(OBJECTS_FOLDER_NAME);
        references = directory.resolve(REFERENCES_FOLDER_NAME);
    }

    /**
     * Find an existing repository in given directory or its parents.
     * @param path Directory, where should be found repository.
     * @return A repository object if found it, {@code null} if did not found.
     * @throws DirectoryExpectedException If given path is not a directory.
     * @throws IOException If something went wrong with files or paths.
     */
    static @Nullable Repository findRepository(@NotNull Path path)
            throws DirectoryExpectedException, IOException {
        while (path != null) {
            Path repositoryDirectory = path.resolve(REPOSITORY_DIRECTORY_NAME);
            if (Files.exists(repositoryDirectory)) {
                return new Repository(repositoryDirectory);
            }
            path = path.getParent();
        }
        return null;
    }

    /**
     * Inits repository in given directory.
     * @param path Path where will new repository be inited.
     * @return Created repository object.
     * @throws GitAlreadyInitializedException Throws when user tries to reinitialize one repository.
     * @throws DirectoryExpectedException When given path is not a directory.
     * @throws IOException Ig something goes wrong with files or paths.
     */
    static @NotNull Repository createRepository(@NotNull Path path)
            throws GitAlreadyInitializedException, DirectoryExpectedException, IOException{
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        if (findRepository(path) != null) {
            throw new GitAlreadyInitializedException("");
        }
        Path directory = path.resolve(REPOSITORY_DIRECTORY_NAME);
        Files.createDirectory(directory);
        Files.createDirectory(directory.resolve(OBJECTS_FOLDER_NAME));
        Files.createDirectory(directory.resolve(REFERENCES_FOLDER_NAME));
        Files.createFile(directory.resolve(INDEX_FILE_NAME));
        Files.createFile(directory.resolve(HEAD_FILE_NAME));
        return new Repository(directory);
    }

    /**
     * Find initialized repository in given directory.
     * @param path Directory where should repository be located.
     * @param startFromParent {@code true} if repository should be found in parent directory.
     * @return If searching of repository should be started from parent directory.
     * @throws GitNotInitializedException Throws when found no MyGit repository.
     * @throws IOException Throws when something wrong with file system.
     * @throws DirectoryExpectedException Throws when given directory is not a directory.
     */
    static @NotNull Repository resolveRepository(@NotNull Path path, boolean startFromParent)
            throws GitNotInitializedException, IOException, DirectoryExpectedException {
        Repository repository;
        if (startFromParent && path.getParent() == null) {
            throw new GitNotInitializedException("");
        }
        repository = findRepository(startFromParent ? path.getParent() : path);
        if (repository == null) {
            throw new GitNotInitializedException("");
        }
        return repository;
    }

    /**
     * Checking whether in repository could be an object with given SHA-1 hash.
     * @param sha Given SHA-1 hash.
     * @return {@code true} if there could be an object in repository with this hash, {@code false} otherwise.
     */
    boolean isValidSha(@NotNull String sha) {
        return sha.length() > OBJECTS_FOLDER_NAME_LENGTH && Files.exists(getObject(sha));
    }

    /**
     * Returns path to object with given SHA-1 hash from this repository.
     * @param sha SHA-1 hash of needed GitObject.
     * @return Path to object found as {@code Path} object.
     */
    Path getObject(@NotNull String sha) {
        Path subDirectory = getObjects().resolve(sha.substring(0, OBJECTS_FOLDER_NAME_LENGTH));
        return subDirectory.resolve(sha.substring(OBJECTS_FOLDER_NAME_LENGTH));
    }

    /**
     * Get current HEAD of this repository.
     * @return Current head as {@code String} object.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     */
    @NotNull String getCurrentHead() throws IOException, HeadFileFailedException {
        if (!Files.exists(getHEAD())) {
            throw new HeadFileFailedException("Head file does not exist.");
        }
        List<String> headLines = Files.readAllLines(getHEAD());
        if (headLines.size() == 0) {
            return REFERENCE_HEAD_PREFIX + DEFAULT_BRANCH_NAME;
        } else if (headLines.size() > 1) {
            throw new HeadFileFailedException("HEAD size is too big.");
        } else {
            String head = headLines.get(0);
            if (head.startsWith(REFERENCE_HEAD_PREFIX)) {
                String branchName = head.substring(REFERENCE_HEAD_PREFIX.length());
                if (!Files.exists(getReferences().resolve(branchName))) {
                    throw new HeadFileFailedException("Reference directory does not exist.");
                }
            } else {
                if (!isValidSha(head)) {
                    throw new HeadFileFailedException("Not valid SHA-1 hash of head reference.");
                }
            }
            return head;
        }
    }

    /**
     * Makes repository kill itself..
     * @throws IOException Throws when something is wrong with file system.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     */
    void removeRepository() throws
            IOException, DirectoryExpectedException{
        FileUtils.deleteDirectory(directory.toFile());
    }

    /**
     * Get sha of revision with given name.
     * @param revision Name of revision.
     * @return SHA-1 hash of revision.
     * @throws IOException Trows when something is wrong with file system.
     * @throws NoSuchRevisionException Throws when trying to merge revision that does not exist.
     */
    String getMergedRevisionSha(@NotNull String revision) throws IOException, NoSuchRevisionException{
        String mergedRevisionSha;
        Path possibleReference = references.resolve(revision);
        if (Files.exists(possibleReference)) {
            mergedRevisionSha = Files.readAllLines(possibleReference).get(0);
            if (!isValidSha(mergedRevisionSha)) {
                throw new NoSuchRevisionException("SHA-1 of revision is invalid.");
            }
        } else {
            mergedRevisionSha = revision;
            if (!isValidSha(mergedRevisionSha)) {
                throw new NoSuchRevisionException("");
            }
        }
        return mergedRevisionSha;
    }

    /**
     * Add any GitObject to repository.
     * @param object What we want to add.
     * @return SHA-1 hash of given object.
     * @throws IOException Throws if something go wrong with file system.
     */
    String addGitObject(@NotNull GitObject object)
            throws IOException {
        Path objectDirectory = objects.resolve(object.getSha().substring(0, OBJECTS_FOLDER_NAME_LENGTH));
        if (!Files.exists(objectDirectory)) {
            Files.createDirectory(objectDirectory);
        }
        Files.write(objectDirectory.resolve(object.getSha().substring(OBJECTS_FOLDER_NAME_LENGTH)),
                object.getContent());
        return object.getSha();
    }

    /**
     * Read index file and return {@code Map<Path, String>} with its content.
     * @return {@code Map<Path, String>} with content of index file.
     * @throws IndexFileFailedException Throws if reading index file failed.
     * @throws IOException Throws if something goes wrong with file system.
     */
    @NotNull Map<Path, String> readIndexFile()
            throws IndexFileFailedException, IOException {
        Map<Path, String> indexContent = new HashMap<>();
        Splitter onTabSplitter = Splitter.on('\t').omitEmptyStrings().trimResults();
        if (!Files.exists(index)) {
            throw new IndexFileFailedException("Index file does not exist.");
        }
        for (String line : Files.readAllLines(index)) {
            List<String> splitResult = onTabSplitter.splitToList(line);
            if (splitResult.size() != 2 || !isValidSha(splitResult.get(1))) {
                throw new IndexFileFailedException("Incorrect line in index file.");
            }
            try {
                indexContent.put(Paths.get(splitResult.get(0)), splitResult.get(1));
            } catch (Exception e) {
                throw new IndexFileFailedException("No such directory: " + splitResult.get(0));
            }
        }
        return indexContent;
    }

    /**
     * Reads commit from objects file by its SHA-1 hash.
     * @param commit SHA-1 hash of commit.
     * @return Commit and all its information as {@code Commit} object.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws IOException Throws when something is wrong with file system.
     */
    @NotNull Commit getCommit(@NotNull String commit)
            throws InvalidCommitException, IOException {
        if (!isValidSha(commit)) {
            throw new InvalidCommitException("Invalid SHA-1 hash of repository.");
        }
        List<String> commitLines = Files.readAllLines(getObject(commit));
        if (commitLines.size() < 4) {
            throw new InvalidCommitException("Commit file is too small.");
        }
        if (!isValidSha(commitLines.get(0))) {
            throw new InvalidCommitException("Invalid SHA-1 hash of object inside of commit file.");
        }
        Date date = new Date(Long.parseLong(commitLines.get(2)));
        int messageStartLineIndex = 3;
        while (messageStartLineIndex < commitLines.size() &&
                !commitLines.get(messageStartLineIndex).startsWith(MESSAGE_COMMIT_PREFIX)) {
            messageStartLineIndex++;
        }
        if (messageStartLineIndex == commitLines.size()) {
            throw new InvalidCommitException("No message in commit.");
        }
        StringBuilder messageBuilder = new StringBuilder(commitLines.get(messageStartLineIndex)
                .substring(MESSAGE_COMMIT_PREFIX.length()));
        for (int i = messageStartLineIndex + 1; i < commitLines.size(); ++i) {
            messageBuilder.append('\n');
            messageBuilder.append(commitLines.get(i));
        }
        String message = messageBuilder.toString();
        Commit nextCommit = new Commit(commitLines.get(0), message, new ArrayList<>(), commitLines.get(1), date);
        List<String> parentSet = nextCommit.getParents();
        for (int i = 3; i < messageStartLineIndex; ++i) {
            String line = commitLines.get(i);
            if (!line.startsWith(PARENT_COMMIT_PREFIX) ||
                    !isValidSha(line.substring(PARENT_COMMIT_PREFIX.length()))) {
                throw new InvalidCommitException("Wrong format of file.");
            }
            parentSet.add(line.substring(PARENT_COMMIT_PREFIX.length()));
        }
        return nextCommit;
    }

    /**
     * Update index file up to contemporary content of repository.
     * @throws IOException Throws when something goes wrong with file system.
     * @throws IndexFileFailedException Throws when reading or writing to index file failed.
     */
    void writeToIndex(@NotNull List<String> indexContent) throws IOException, IndexFileFailedException{
        Files.write(index, indexContent);
    }

    /**
     * Gives revision SHA by its name.
     * @param name Name of branch or file SHA-1 hash of which we want to get.
     * @throws IOException Throws when something is wrong with file system.
     * @throws NoSuchRevisionException Throws when trying to checkout to version which does not exist.
     */
    String getNewRevision(String name) throws IOException, NoSuchRevisionException{
        String revision;
        Path possibleReference = references.resolve(name);
        if (Files.exists(possibleReference)) {
            revision = Files.readAllLines(possibleReference).get(0);
            if (!isValidSha(revision)) {
                throw new NoSuchRevisionException("SHA-1 hash of revision is invalid.");
            }
        } else {
            revision = name;
            if (!isValidSha(revision)) {
                throw new NoSuchRevisionException("SHA-1 hash of revision is invalid.");
            }
        }
        return revision;
    }

    /**
     * Gives current revision from HEAD file.
     * @return SHA-1 hash of current revision.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     */
    String getCurrentRevision() throws HeadFileFailedException, IOException{
        String currentHead = getCurrentHead();
        String currentRevision;
        if (currentHead.startsWith(REFERENCE_HEAD_PREFIX)) {
            String currentBranch = currentHead.substring(REFERENCE_HEAD_PREFIX.length());
            Path reference = getReferences().resolve(currentBranch);
            if (!Files.exists(reference)) {
                throw new HeadFileFailedException("File does not exist " + reference.toString());
            }
            currentRevision = Files.readAllLines(reference).get(0);
        } else {
            currentRevision = currentHead;
        }
        return currentRevision;
    }

}