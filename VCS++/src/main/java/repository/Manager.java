package repository;

import com.google.common.base.Splitter;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import exceptions.*;
import git_objects.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * repository.Manager of MyGit repository.
 */
public class Manager {

    /**
     * Find an existing repository in given directory or its parents.
     * @param path Directory, where should be found repository.
     * @return A repository object if found it, {@code null} if did not found.
     * @throws DirectoryExpectedException If given path is not a directory.
     * @throws IOException If something went wrong with files or paths.
     */
    public static @Nullable Repository findRepository(@NotNull Path path)
            throws DirectoryExpectedException, IOException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        if (!Files.isDirectory(path)) {
            throw new DirectoryExpectedException("");
        }
        return Repository.findRepository(path);
    }

    /**
     * Inits new repository in given directory. Fills it with all needed files.
     * @param path Directory in which new repository should be inited.
     * @return Repository object referred to inited repository.
     * @throws IOException Throws when something goes wrong with paths.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws GitAlreadyInitializedException Throws when trying to reinit already created repository.
     */
    public static @NotNull
    Repository init(@NotNull Path path)
            throws IOException, DirectoryExpectedException, GitAlreadyInitializedException,
            HeadFileFailedException, GitNotInitializedException, BranchAlreadyExistException {
        return  Repository.createRepository(path);
    }

    /**
     * Add file to repository. Makes Blob object of given file and add it to given repository.
     * @param repository Repository in which file should be added.
     * @param filePath Path to file which is wanted to be added.
     * @return SHA-1 hash of Blob object of given file.
     * @throws IOException Throws if something goes wrong with file system.
     */
    public static String addFile(@NotNull Repository repository, @NotNull Path filePath) throws IOException {
        return repository.addGitObject(new Blob(Files.readAllBytes(filePath), filePath.getFileName().toString()));
    }

    /**
     * Read index file of given repository and return {@code Map<Path, String>} with its content.
     * @param repository Index file of this repository would be read.
     * @return {@code Map<Path, String>} with content of index file.
     * @throws IndexFileFailedException Throws if reading index file failed.
     * @throws IOException Throws if something goes wrong with file system.
     */
    private static @NotNull Map<Path, String> readIndexFile(@NotNull Repository repository)
            throws IndexFileFailedException, IOException {
        return repository.readIndexFile();
    }

    /**
     * Update index file up to contemporary content of repository.
     * @param repository Index file of which repository would be updated.
     * @param addedFiles Which files where added to repository since last update in format of {@code Map}.
     * @param removedFiles Which where removed from repository since last update as list of {@code Path} objects.
     * @throws IOException Throws when something goes wrong with file system.
     * @throws IndexFileFailedException Throws when reading or writing to index file failed.
     */
    private static void updateIndex(@NotNull Repository repository, @NotNull Map<Path, String> addedFiles,
                                    @NotNull Set<Path> removedFiles) throws IOException, IndexFileFailedException{
        Map<Path, String> index = readIndexFile(repository);
        index.keySet().removeAll(removedFiles);
        index.putAll(addedFiles);
        repository.writeToIndex(index.entrySet().stream()
                                .map(entry -> entry.getKey().toString() + '\t' + entry.getValue())
                                .sorted()
                                .collect(Collectors.toList()));
    }

    /**
     * Add information about Blob file to index file of current repository.
     * @param path Path of file that should be added.
     * @throws GitNotInitializedException Throws when trying to work with repository that was not inited.
     * @throws IOException Throws when something is wrong with file system.
     * @throws IndexFileFailedException Trows when there are problems with reading or writing to index file.
     * @throws DirectoryExpectedException Throws when {@code Repository.resolveRepository}
     *         throws DirectoryExpectedException. Given path does not name a directory.
     */
    public static void addToIndex(@NotNull Path path) throws GitNotInitializedException, IOException,
            IndexFileFailedException, DirectoryExpectedException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        Repository repository = Repository.resolveRepository(path, true);
        Map<Path, String> addedFiles = new HashMap<>();
        Files.walk(path).forEach(filePath -> {
            filePath = filePath.toAbsolutePath().normalize();
            if (Files.isRegularFile(filePath) && !filePath.startsWith(repository.getDirectory())) {
                try{
                    addedFiles.put(repository.getDirectory().getParent().relativize(filePath), addFile(repository, filePath));
                } catch (IOException e){
                    //Oh no! Exception inside lambda!
                }
            }
        });
        updateIndex(repository, addedFiles, Collections.emptySet());
    }

    /**
     * Remove information about Blob file from index file of current repository.
     * @param path Path of repository that should be found. Its index file would be remade.
     * @throws GitNotInitializedException Throws when trying to work with repository that was not inited.
     * @throws IOException Throws when something is wrong with file system.
     * @throws IndexFileFailedException Trows when there are problems with reading or writing to index file.
     * @throws DirectoryExpectedException Throws when {@code Repository.resolveRepository}
     *         throws DirectoryExpectedException. Given path does not name a directory.
     */
    public static void removeFromIndex(@NotNull Path path) throws IOException, GitNotInitializedException,
            IndexFileFailedException, DirectoryExpectedException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        Repository repository = Repository.resolveRepository(path, true);
        Set<Path> removedFiles = new HashSet<>();
        Files.walk(path).forEach(filePath -> {
            filePath = filePath.toAbsolutePath().normalize();
            if (Files.isRegularFile(filePath) && !filePath.startsWith(repository.getDirectory())) {
                removedFiles.add(repository.getDirectory().getParent().relativize(filePath));
            }
        });
        Files.delete(path);
        updateIndex(repository, Collections.emptyMap(), removedFiles);
    }

    /**
     * Built tree of repository by its index file.
     * @param repository Tree of which repository would be built.
     * @return Tree of repository as {@code Tree} object.
     * @throws IndexFileFailedException Trows when reading of index file failed.
     * @throws IOException Throws when something goes wrong with file system.
     */
    private static @NotNull Tree collectTreeFromIndex(@NotNull Repository repository)
            throws IndexFileFailedException, IOException {
        Map<Path, String> index = readIndexFile(repository);
        Map<Path, Tree> pathToTree = new HashMap<>();
        pathToTree.put(Paths.get(""), new Tree(""));
        for (Path path : index.keySet()) {
            for (Path prefixPath : path) {
                Path parent = prefixPath.getParent();
                if (parent == null) {
                    parent = Paths.get("");
                }
                Path name = prefixPath.getFileName();
                if (prefixPath.equals(path)) {
                    pathToTree.get(parent).addChild(
                            new GitObjectNamed(name.toString(), GitObjectType.BLOB, index.get(path)));
                } else if (!pathToTree.containsKey(prefixPath)) {
                    pathToTree.put(prefixPath, new Tree(name.toString()));
                }
            }
        }
        return pathToTree.get(Paths.get(""));
    }

    /**
     * Make new commit with changes.
     * @param repository Repository whose changes should be committed.
     * @param message Message of new commit.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws IndexFileFailedException Throws when reading of index file failed.
     */
    public static void commit(@NotNull Repository repository, @NotNull String message, @NotNull String extraParent)
            throws IOException, HeadFileFailedException,
            DirectoryExpectedException, IndexFileFailedException {
        String currentHead = repository.getCurrentHead();
        if (currentHead.startsWith(GitConstants.REFERENCE_HEAD_PREFIX)) {
            String currentBranch = currentHead.substring(GitConstants.REFERENCE_HEAD_PREFIX.length());
            String parent = null;
            Path reference = repository.getReferences().resolve(currentBranch);
            if (Files.exists(reference)) {
                parent = Files.readAllLines(reference).get(0);
            }
            Tree tree = collectTreeFromIndex(repository);
            repository.addGitObject(tree);
            Commit commit = new Commit(tree.getSha(), message, new ArrayList<>(),
                    System.getProperty(GitConstants.USER_NAME_PROPERTY), new Date());
            if (parent != null) {
                commit.addParent(parent);
            }
            if (extraParent != null) {
                commit.addParent(extraParent);
            }
            repository.addGitObject(commit);
            if (!Files.exists(reference)) {
                Files.createFile(reference);
            }
            Files.write(reference, commit.getSha().getBytes());
        } else {
            Tree tree = collectTreeFromIndex(repository);
            repository.addGitObject(tree);
            Commit commit = new Commit(tree.getSha(), message, new ArrayList<>(),
                    System.getProperty(GitConstants.USER_NAME_PROPERTY), new Date());
            commit.addParent(currentHead);
            if (extraParent != null) {
                commit.addParent(extraParent);
            }
            Files.write(repository.getHEAD(), repository.addGitObject(commit).getBytes());
        }
    }

    /**
     * Create new branch in given directory.
     * @param repository Repository in which should be new branch created.
     * @param branchName Name of new branch.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws BranchAlreadyExistException Throws when trying to create branch that already exists.
     */
    public static void newBranch(@NotNull Repository repository, @NotNull String branchName) throws IOException,
            DirectoryExpectedException, HeadFileFailedException, BranchAlreadyExistException{
        Path newReference = repository.getReferences().resolve(branchName);
        if (Files.exists(newReference)) {
            throw new BranchAlreadyExistException("");
        }
        String currentHead = repository.getCurrentHead();
        if (currentHead.startsWith(GitConstants.REFERENCE_HEAD_PREFIX)) {
            String currentBranch = currentHead.substring(GitConstants.REFERENCE_HEAD_PREFIX.length());
            Files.copy(repository.getReferences().resolve(currentBranch), newReference);
        } else {
            Files.write(newReference, currentHead.getBytes());
        }
        Files.write(repository.getHEAD(), (GitConstants.REFERENCE_HEAD_PREFIX + branchName).getBytes());
    }

    /**
     * Returns name of current branch.
     * @param repository Repository current branch of which should be found.
     * @return {@code String} object with name of current branch.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     */
    public static String currentBranch(@NotNull Repository repository)
            throws IOException, DirectoryExpectedException, HeadFileFailedException {
        String currentHead = repository.getCurrentHead();
        if (currentHead.startsWith(GitConstants.REFERENCE_HEAD_PREFIX)) {
            return currentHead.substring(GitConstants.REFERENCE_HEAD_PREFIX.length());
        }
        return null;
    }

    /**
     * Delete Branch by its name.
     * @param repository Repository in which branch should be removed.
     * @param branchName Name of branch wanted to be deleted.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws BranchDoesNotExistException Throws when trying to delete branch which does not exist.
     * @throws CanNotDeleteBranchException Throws when trying to delete branch which HEAD file points on.
     */
    public static void deleteBranch(@NotNull Repository repository, @NotNull String branchName) throws IOException,
            DirectoryExpectedException, HeadFileFailedException,
            BranchDoesNotExistException, CanNotDeleteBranchException{
        Path pastReference = repository.getReferences().resolve(branchName);
        if (!Files.exists(pastReference)) {
            throw new BranchDoesNotExistException("");
        }
        String currentHead = repository.getCurrentHead();
        if (currentHead.startsWith(GitConstants.REFERENCE_HEAD_PREFIX)) {
            if (currentHead.equals(GitConstants.REFERENCE_HEAD_PREFIX + branchName)) {
                throw new CanNotDeleteBranchException("");
            }
        }
        Files.delete(pastReference);
    }

    /**
     Checkouts a revision and moves HEAD there.
     * @param repository Repository in which checkout should be done.
     * @param name Name of branch or file on version of which we want to switch.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws NoSuchRevisionException Throws when trying to checkout to version which does not exist.
     * @throws IndexFileFailedException Throws when reading of index file failed.
     * @throws InvalidTreeException Throws when tree file format is incorrect.
     */
    public static void checkout(@NotNull Repository repository, @NotNull String name)
            throws IOException, DirectoryExpectedException, InvalidCommitException, HeadFileFailedException,
            NoSuchRevisionException, IndexFileFailedException, InvalidTreeException, IndexFileNotEmptyException {
        String newRevision = repository.getNewRevision(name);
        String currentRevision = repository.getCurrentRevision();
        if (!repository.isValidSha(currentRevision)) {
            throw new NoSuchRevisionException("SHA-1 hash of revision is invalid.");
        }
        Commit currentRevisionCommit = readCommit(repository, currentRevision);
        Commit newRevisionCommit = readCommit(repository, newRevision);
        Tree currentTree = readTree(repository, currentRevisionCommit.getTree());
        Tree newTree = readTree(repository, newRevisionCommit.getTree());
        Set<Path> removedFiles = walkVcsTree(currentTree).keySet();
        Map<Path, String> addedFiles = walkVcsTree(newTree);
        for (Path removedFile : removedFiles) {
            Files.delete(repository.getDirectory().getParent().resolve(removedFile));
        }
        for (Map.Entry<Path, String> entry : addedFiles.entrySet()) {
            Path addedFile = entry.getKey();
            String sha = entry.getValue();
            if (!repository.isValidSha(sha)) {
                throw new NoSuchRevisionException("SHA-1 hash of revision is invalid");
            }
            Files.copy(repository.getObject(sha), repository.getDirectory().getParent().resolve(addedFile));
        }
        updateIndex(repository, addedFiles, removedFiles);
        Files.write(repository.getHEAD(),
                ((Files.exists(repository.getReferences().resolve(name)) ? GitConstants.REFERENCE_HEAD_PREFIX
                                                                         : "") + name).getBytes());
    }

    /**
     * Reads commit from objects file by its SHA-1 hash.
     * @param repository Repository in which commit was created.
     * @param commit SHA-1 hash of commit.
     * @return Commit and all its information as {@code Commit} object.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws IOException Throws when something is wrong with file system.
     */
    private static @NotNull Commit readCommit(@NotNull Repository repository, @NotNull String commit)
            throws InvalidCommitException, IOException {
        return repository.getCommit(commit);
    }

    /**
     * Reads tree of whole given repository.
     * @param repository Tree of which repository should be read.
     * @param tree SHA-1 hash of result tree.
     * @return Tree of repository as {@code Tree} object.
     * @throws IOException Throws when something is wrong with file system.
     * @throws InvalidTreeException Throws when tree file format is incorrect.
     */
    private static @NotNull Tree readTree(@NotNull Repository repository, @NotNull String tree)
            throws InvalidTreeException, IOException {
        return readTree(repository, tree, "");
    }

    /**
     * Reads subtree tree of given repository.
     * @param repository Repository in which tree is located.
     * @param tree SHA-1 hash of the tree.
     * @param treeName Name of the tree.
     * @return Subtree of repository as {@code Tree} object.
     * @throws IOException Throws when something is wrong with file system.
     * @throws InvalidTreeException Throws when tree file format is incorrect.
     */
    private static @NotNull Tree readTree(@NotNull Repository repository,
                                          @NotNull String tree, @NotNull String treeName)
            throws InvalidTreeException, IOException {
        if (!repository.isValidSha(tree)) {
            throw new InvalidTreeException("Invalid SHA-1 hash of tree.");
        }
        Tree answerTree = new Tree(treeName);
        Splitter onTabSplitter = Splitter.on('\t').omitEmptyStrings().trimResults();
        for (String line : Files.readAllLines(repository.getObject(tree))) {
            List<String> splitResults = onTabSplitter.splitToList(line);
            if (splitResults.size() != 3) {
                throw new InvalidTreeException("Size of tree file is too small.");
            }
            GitObjectType type;
            try {
                type = GitObjectType.valueOf(splitResults.get(0));
            } catch (IllegalArgumentException e) {
                throw new InvalidTreeException("Unsupported type of file inside tree.");
            }
            if (!(type == GitObjectType.BLOB || type == GitObjectType.TREE)) {
                throw new InvalidTreeException("Unsupported type of file inside tree.");
            }
            String sha = splitResults.get(1);
            String name = splitResults.get(2);
            if (type == GitObjectType.BLOB) {
                if (!repository.isValidSha(sha)) {
                    throw new InvalidTreeException("Invalid SHA-1 hash of tree.");
                }
                answerTree.addChild(new GitObjectNamed(name, type, sha));
            } else {
                answerTree.addChild(readTree(repository, sha, treeName));
            }
        }
        return answerTree;
    }

    /**
     * Walk through all the given tree and returns all files as pairs of path and name.
     * @param tree Tree given.
     * @return Pairs of path and name of all files in the tree as {@code Map<Path, String>} object.
     */
    private static @NotNull Map<Path, String> walkVcsTree(@NotNull Tree tree) {
        Map<Path, String> files = new HashMap<>();
        walkVcsTree(tree, Paths.get(""), files);
        return files;
    }

    /**
     * Recursive function. Walk through given tree and improve some HashMap with pairs of path and name of all files.
     * @param tree Given tree to walk around.
     * @param path Current position in the tree.
     * @param filesMap Reference on hash map with results.
     */
    private static void walkVcsTree(@NotNull Tree tree, @NotNull Path path, @NotNull Map<Path, String> filesMap) {
        for (GitObject object : tree.getChildren()) {
            if (object.getType() == GitObjectType.BLOB) {
                filesMap.put(path.resolve(((GitObjectNamed) object).getName()), object.getSha());
            } else {
                walkVcsTree((Tree) object, path.resolve(((GitObjectNamed) object).getName()), filesMap);
            }
        }
    }

    /**
     * Return all branches in MyGit repository.
     * @param repository Repository branches of which should be listed.
     * @return {@code List<String>} with names of all branches.
     * @throws IOException Throws when something is wrong with file system.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     */
    public static List<String> allBranches(@NotNull Repository repository)
            throws IOException, DirectoryExpectedException {
        Path reference = repository.getReferences();
        List<String> ans = Files.walk(reference).map(Path::toString).collect(Collectors.toList());
        return ans.subList(1, ans.size());
    }

    /**
     * Merge current branch (revision) and given branch(revision).
     * @param repository Repository branches of which should be merged.
     * @param revisionName Second revision name, which current should be merged to.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws NoSuchRevisionException Throws when trying to checkout to version which does not exist.
     * @throws IndexFileFailedException Throws when reading of index file failed.
     * @throws InvalidTreeException Throws when tree file format is incorrect.
     */
    public static void merge(@NotNull Repository repository, @NotNull String revisionName) throws IOException,
            DirectoryExpectedException, HeadFileFailedException, IndexFileFailedException, NoSuchRevisionException,
            InvalidTreeException, InvalidCommitException {
        String mergedRevision = repository.getMergedRevisionSha(revisionName);
        String currentHead = repository.getCurrentHead();
        String currentRevision;
        if (currentHead.startsWith(GitConstants.REFERENCE_HEAD_PREFIX)) {
            String currentBranch = currentHead.substring(GitConstants.REFERENCE_HEAD_PREFIX.length());
            Path reference = repository.getReferences().resolve(currentBranch);
            if (!Files.exists(reference)) {
                throw new HeadFileFailedException("");
            }
            currentRevision = Files.readAllLines(reference).get(0);
        } else {
            currentRevision = currentHead;
        }
        if (!repository.isValidSha(currentRevision)) {
            throw new NoSuchRevisionException("SHA-1 of revision is incorrect.");
        }
        Set<Path> removedFiles = Collections.emptySet();
        Commit deployedRevision = readCommit(repository, mergedRevision);
        Tree deployedTree = readTree(repository, deployedRevision.getTree());
        Map<Path, String> addedFiles = walkVcsTree(deployedTree);
        for (Map.Entry<Path, String> entry : addedFiles.entrySet()) {
            Path addedFile = entry.getKey();
            String sha = entry.getValue();
            if (!repository.isValidSha(sha)) {
                throw new InvalidTreeException("SHA-1 hash of an object is invalid.");
            }
            Files.copy(repository.getObject(sha), repository.getDirectory().resolve(addedFile), REPLACE_EXISTING);
        }
        updateIndex(repository, addedFiles, removedFiles);
        commit(repository, GitConstants.MERGE_COMMIT_PREFIX + revisionName, mergedRevision);
    }

    /**
     * Remove repository in given directory.
     * @param repository Repository that should be removed.
     * @throws IOException Throws when something is wrong with file system.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     */
    public static void removeRepository(@NotNull Repository repository) throws
            IOException, DirectoryExpectedException{
        repository.removeRepository();
    }

    /**
     * Get log of all commits, attached to current head revision.
     * @param path Path to given commit.
     * @return Log message with information about commit as {@code Log} object.
     * @throws IOException Throws when something is wrong with file system.
     * @throws GitNotInitializedException Throws when there if no any repository at such path.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws DirectoryExpectedException Throws when given path is not a directory
     */
    public static @Nullable Log getLog(@NotNull Repository repository) throws IOException, GitNotInitializedException,
            DirectoryExpectedException, InvalidCommitException, HeadFileFailedException{
        String startCommit = repository.getCurrentRevision();
        Commit headCommit = readCommit(repository, startCommit);
        Set<String> presentSet = new HashSet<>();
        presentSet.add(startCommit);
        List<Commit> commits = getCommitsDFS(repository, presentSet, new ArrayList<>(), headCommit);
        Log currentLogMessage = null;
        for (Commit commit : commits) {
            currentLogMessage = new Log(commit, currentLogMessage);
        }
        return currentLogMessage;
    }

    /**
     * Get all commits by bfs sorted be time of creation.
     * @param repository Repository in which commits should be found.
     * @param presentSet SHA-1 hashes of all found commits.
     * @param commits List of all found commits.
     * @param currentCommit Commit whose parents should be examined the next moment.
     * @return List of all found commits.
     * @throws IOException Throws when something is wrong with file system.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     */
    private static @Nullable List<Commit> getCommitsDFS(Repository repository, Set<String> presentSet,
                                                        List<Commit> commits, Commit currentCommit)
            throws IOException, InvalidCommitException{
        commits.add(currentCommit);
        for (String parent : currentCommit.getParents()) {
            if (!presentSet.contains(parent)) {
                presentSet.add(parent);
                getCommitsDFS(repository, presentSet, commits, readCommit(repository, parent));
            }
        }
        commits.sort(Comparator.comparingLong(Commit::getDateMilliseconds));
        return commits;
    }
}