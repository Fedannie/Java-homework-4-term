package repository;

import com.google.common.base.Splitter;
import exceptions.*;
import git_objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Manager of MyGit repository.
 * Managing class that provides all the VCS functionality.
 */
public class Manager {
    /**Keeps some information about work process.*/
    private static final Logger logger = LoggerFactory.getLogger(Manager.class);
    /**Marker of fatal error.*/
    private static final Marker errorMarker = MarkerFactory.getMarker("ERROR");

    /**Repository, which is managed by this {@code Manager} object.*/
    private Repository repository;

    /**
     * Find an existing repository in given directory or its parents and save it
     * in private {@code Repository} object of this class.
     * @param path Directory, where should be found repository.
     * @throws DirectoryExpectedException If given path is not a directory.
     * @throws IOException If something went wrong with files or paths.
     */
    public Manager(@NotNull Path path) throws DirectoryExpectedException, IOException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        if (!Files.isDirectory(path)) {
            logger.error(errorMarker, "Expected directory but got: '{}'", path);
            throw new DirectoryExpectedException("");
        }
        repository = Repository.findRepository(path);
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
    Repository init(@NotNull Path path) throws IOException, DirectoryExpectedException, GitAlreadyInitializedException,
            HeadFileFailedException, GitNotInitializedException, BranchAlreadyExistException {
        logger.debug("Creating new repository.");
        return  Repository.createRepository(path);
    }

    /**
     * Add file to repository. Makes Blob object of given file and add it to given repository.
     * @param filePath Path to file which is wanted to be added.
     * @return SHA-1 hash of Blob object of given file.
     * @throws IOException Throws if something goes wrong with file system.
     */
    public String addFile(@NotNull Path filePath) throws IOException {
        logger.debug("Add new file: '{}'", filePath.toString());
        return repository.addGitObject(new Blob(Files.readAllBytes(filePath), filePath.getFileName().toString()));
    }

    /**
     * Read index file of current repository and return {@code Map<Path, String>} with its content.
     * @return {@code Map<Path, String>} with content of index file.
     * @throws IndexFileFailedException Throws if reading index file failed.
     * @throws IOException Throws if something goes wrong with file system.
     */
    private @NotNull Map<Path, String> readIndexFile()
            throws IndexFileFailedException, IOException {
        logger.debug("Reading index file.");
        return repository.readIndexFile();
    }

    /**
     * Update index file up to contemporary content of repository.
     * @param addedFiles Which files where added to repository since last update in format of {@code Map}.
     * @param removedFiles Which where removed from repository since last update as list of {@code Path} objects.
     * @throws IOException Throws when something goes wrong with file system.
     * @throws IndexFileFailedException Throws when reading or writing to index file failed.
     */
    private void updateIndex(@NotNull Map<Path, String> addedFiles,
                             @NotNull Set<Path> removedFiles) throws IOException, IndexFileFailedException{
        logger.debug("Updating index file.");
        Map<Path, String> index = readIndexFile();
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
    public void addToIndex(@NotNull Path path) throws GitNotInitializedException, IOException,
            IndexFileFailedException, DirectoryExpectedException {
        logger.debug("Add file to index: '{}'", path.toString());
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        Map<Path, String> addedFiles = new HashMap<>();
        Files.walk(path).forEach(filePath -> {
            filePath = filePath.toAbsolutePath().normalize();
            if (Files.isRegularFile(filePath) && !filePath.startsWith(repository.getDirectory())) {
                try{
                    addedFiles.put(getRoot().relativize(filePath), addFile(filePath));
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        updateIndex(addedFiles, Collections.emptySet());
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
    public void removeFromIndex(@NotNull Path path) throws IOException, GitNotInitializedException,
            IndexFileFailedException, DirectoryExpectedException {
        logger.debug("Remove file from index file: '{}'", path.toString());
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        Set<Path> removedFiles = new HashSet<>();
        Files.walk(path).forEach(filePath -> {
            filePath = filePath.toAbsolutePath().normalize();
            if (Files.isRegularFile(filePath) && !filePath.startsWith(repository.getDirectory())) {
                removedFiles.add(getRoot().relativize(filePath));
            }
        });
        Files.delete(path);
        updateIndex(Collections.emptyMap(), removedFiles);
    }

    /**
     * Built tree of repository by its index file.
     * @return Tree of repository as {@code Tree} object.
     * @throws IndexFileFailedException Trows when reading of index file failed.
     * @throws IOException Throws when something goes wrong with file system.
     */
    private @NotNull Tree collectTreeFromIndex()
            throws IndexFileFailedException, IOException {
        logger.debug("Start reading tree from index file.");
        Map<Path, String> index = readIndexFile();
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
     * @param message Message of new commit.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws IndexFileFailedException Throws when reading of index file failed.
     */
    public void commit(@NotNull String message, @Nullable String extraParent)
            throws IOException, HeadFileFailedException,
            DirectoryExpectedException, IndexFileFailedException {
        logger.debug("Creating new commit: '{}'", message);
        String currentHead = repository.getCurrentHead();
        if (currentHead.startsWith(Repository.REFERENCE_HEAD_PREFIX)) {
            String currentBranch = currentHead.substring(Repository.REFERENCE_HEAD_PREFIX.length());
            String parent = null;
            Path reference = repository.getReferences().resolve(currentBranch);
            if (Files.exists(reference)) {
                parent = Files.readAllLines(reference).get(0);
            }
            Tree tree = collectTreeFromIndex();
            repository.addGitObject(tree);
            Commit commit = new Commit(tree.getSha(), message, new ArrayList<>(),
                    System.getProperty(Repository.USER_NAME_PROPERTY), new Date());
            if (parent != null) {
                commit.addParent(parent);
            }
            if (extraParent != null){
                commit.addParent(extraParent);
            }
            repository.addGitObject(commit);
            if (!Files.exists(reference)) {
                Files.createFile(reference);
            }
            Files.write(reference, commit.getSha().getBytes());
        } else {
            Tree tree = collectTreeFromIndex();
            repository.addGitObject(tree);
            Commit commit = new Commit(tree.getSha(), message, new ArrayList<>(),
                    System.getProperty(Repository.USER_NAME_PROPERTY), new Date());
            commit.addParent(currentHead);
            commit.addParent(extraParent);
            Files.write(repository.getHEAD(), repository.addGitObject(commit).getBytes());
        }
    }

    /**
     * Create new branch in given directory.
     * @param branchName Name of new branch.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws BranchAlreadyExistException Throws when trying to create branch that already exists.
     */
    public void newBranch(@NotNull String branchName) throws IOException,
            DirectoryExpectedException, HeadFileFailedException, BranchAlreadyExistException{
        logger.debug("Creating new branch: '{}'", branchName);
        Path newReference = repository.getReferences().resolve(branchName);
        if (Files.exists(newReference)) {
            logger.error(errorMarker, "Can not create an already existing branch with name: '{}'", branchName);
            throw new BranchAlreadyExistException("");
        }
        String currentHead = repository.getCurrentHead();
        if (currentHead.startsWith(Repository.REFERENCE_HEAD_PREFIX)) {
            String currentBranch = currentHead.substring(Repository.REFERENCE_HEAD_PREFIX.length());
            Files.copy(repository.getReferences().resolve(currentBranch), newReference);
        } else {
            Files.write(newReference, currentHead.getBytes());
        }
        Files.write(repository.getHEAD(), (Repository.REFERENCE_HEAD_PREFIX + branchName).getBytes());
    }

    /**
     * Returns name of current branch.
     * @return {@code String} object with name of current branch.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     */
    public String currentBranch()
            throws IOException, DirectoryExpectedException, HeadFileFailedException {
        String currentHead = repository.getCurrentHead();
        if (currentHead.startsWith(Repository.REFERENCE_HEAD_PREFIX)) {
            return currentHead.substring(Repository.REFERENCE_HEAD_PREFIX.length());
        }
        return null;
    }

    /**
     * Delete Branch by its name.
     * @param branchName Name of branch wanted to be deleted.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws BranchDoesNotExistException Throws when trying to delete branch which does not exist.
     * @throws CanNotDeleteBranchException Throws when trying to delete branch which HEAD file points on.
     */
    public void deleteBranch(@NotNull String branchName) throws IOException,
            DirectoryExpectedException, HeadFileFailedException,
            BranchDoesNotExistException, CanNotDeleteBranchException{
        logger.debug("Deleting current branch: '{}'", branchName);
        Path pastReference = repository.getReferences().resolve(branchName);
        if (!Files.exists(pastReference)) {
            throw new BranchDoesNotExistException("");
        }
        String currentHead = repository.getCurrentHead();
        if (currentHead.startsWith(Repository.REFERENCE_HEAD_PREFIX)) {
            if (currentHead.equals(Repository.REFERENCE_HEAD_PREFIX + branchName)) {
                throw new CanNotDeleteBranchException("");
            }
        }
        Files.delete(pastReference);
    }

    /**
     Checkouts a revision and moves HEAD there.
     * @param name Name of branch or file on version of which we want to switch.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws NoSuchRevisionException Throws when trying to checkout to version which does not exist.
     * @throws IndexFileFailedException Throws when reading of index file failed.
     * @throws InvalidTreeException Throws when tree file format is incorrect.
     */
    public void checkout(@NotNull String name)
            throws IOException, DirectoryExpectedException, InvalidCommitException, HeadFileFailedException,
            NoSuchRevisionException, IndexFileFailedException, InvalidTreeException{
        logger.debug("Starting checkout revision: '{}'", name);
        String newRevision = repository.getNewRevision(name);
        String currentRevision = repository.getCurrentRevision();
        if (!repository.isValidSha(currentRevision)) {
            throw new NoSuchRevisionException("SHA-1 hash of revision is invalid.");
        }
        Commit currentRevisionCommit = readCommit(currentRevision);
        Commit newRevisionCommit = readCommit(newRevision);
        Tree currentTree = readTree(currentRevisionCommit.getTree());
        Tree newTree = readTree(newRevisionCommit.getTree());
        Set<Path> removedFiles = walkVcsTree(currentTree).keySet();
        Map<Path, String> addedFiles = walkVcsTree(newTree);
        for (Path removedFile : removedFiles) {
            Files.delete(getRoot().resolve(removedFile));
        }
        for (Map.Entry<Path, String> entry : addedFiles.entrySet()) {
            Path addedFile = entry.getKey();
            String sha = entry.getValue();
            if (!repository.isValidSha(sha)) {
                throw new NoSuchRevisionException("SHA-1 hash of revision is invalid");
            }
            Files.copy(repository.getObject(sha), getRoot().resolve(addedFile));
        }
        updateIndex(addedFiles, removedFiles);
        Files.write(repository.getHEAD(),
                ((Files.exists(repository.getReferences().resolve(name)) ? Repository.REFERENCE_HEAD_PREFIX
                                                                         : "") + name).getBytes());
    }

    /**
     * Reads commit from objects file by its SHA-1 hash.
     * @param commit SHA-1 hash of commit.
     * @return Commit and all its information as {@code Commit} object.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws IOException Throws when something is wrong with file system.
     */
    private @NotNull Commit readCommit(@NotNull String commit)
            throws InvalidCommitException, IOException {
        return repository.getCommit(commit);
    }

    /**
     * Reads tree of whole given repository.
     * @param tree SHA-1 hash of result tree.
     * @return Tree of repository as {@code Tree} object.
     * @throws IOException Throws when something is wrong with file system.
     * @throws InvalidTreeException Throws when tree file format is incorrect.
     */
    private @NotNull Tree readTree(@NotNull String tree)
            throws InvalidTreeException, IOException {
        return readTree(tree, "");
    }

    /**
     * Reads subtree of given repository.
     * @param tree SHA-1 hash of the tree.
     * @param treeName Name of the tree.
     * @return Subtree of repository as {@code Tree} object.
     * @throws IOException Throws when something is wrong with file system.
     * @throws InvalidTreeException Throws when tree file format is incorrect.
     */
    private @NotNull Tree readTree(@NotNull String tree, @NotNull String treeName)
            throws InvalidTreeException, IOException {
        logger.debug("Reading subtree of given repository: '{}'", tree);
        if (!repository.isValidSha(tree)) {
            logger.error(errorMarker, "There is no tree with following SHA-1 hash: '{}'", tree);
            throw new InvalidTreeException("Invalid SHA-1 hash of tree.");
        }
        Tree answerTree = new Tree(treeName);
        Splitter onTabSplitter = Splitter.on('\t').omitEmptyStrings().trimResults();
        for (String line : Files.readAllLines(repository.getObject(tree))) {
            List<String> splitResults = onTabSplitter.splitToList(line);
            if (splitResults.size() != 3) {
                String error = "Size of tree file is too small";
                logger.error(errorMarker, error);
                throw new InvalidTreeException(error);
            }
            GitObjectType type;
            try {
                type = GitObjectType.valueOf(splitResults.get(0));
            } catch (IllegalArgumentException e) {
                String error = "Unsupported type of file inside tree.";
                logger.error(errorMarker, error);
                throw new InvalidTreeException(error);
            }
            if (!(type == GitObjectType.BLOB || type == GitObjectType.TREE)) {
                String error = "Unsupported type of file inside tree.";
                logger.error(errorMarker, error);
                throw new InvalidTreeException(error);
            }
            String sha = splitResults.get(1);
            String name = splitResults.get(2);
            if (type == GitObjectType.BLOB) {
                if (!repository.isValidSha(sha)) {
                    logger.error(errorMarker, "There is no tree with SHA-1 following hash: '{}'", tree);
                    throw new InvalidTreeException("Invalid SHA-1 hash of tree.");
                }
                answerTree.addChild(new GitObjectNamed(name, type, sha));
            } else {
                answerTree.addChild(readTree(sha, treeName));
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
        logger.debug("Walking through repository tree.");
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
     * @return {@code List<String>} with names of all branches.
     * @throws IOException Throws when something is wrong with file system.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     */
    public List<String> allBranches() throws IOException, DirectoryExpectedException {
        Path reference = repository.getReferences();
        List<String> ans = Files.walk(reference).map(Path::toString).collect(Collectors.toList());
        return ans.subList(1, ans.size());
    }

    /**
     * Merge current branch (revision) and given branch(revision).
     * @param revisionName Second revision name, which current should be merged to.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws NoSuchRevisionException Throws when trying to checkout to version which does not exist.
     * @throws IndexFileFailedException Throws when reading of index file failed.
     * @throws InvalidTreeException Throws when tree file format is incorrect.
     */
    public void merge(@NotNull String revisionName) throws IOException, DirectoryExpectedException,
            HeadFileFailedException, IndexFileFailedException, NoSuchRevisionException,
            InvalidTreeException, InvalidCommitException {
        logger.debug("Starting merging current branch with: '{}'", revisionName);
        String mergedRevision = repository.getMergedRevisionSha(revisionName);
        String currentRevision = repository.getCurrentRevision();
        if (!repository.isValidSha(currentRevision)) {
            logger.error(errorMarker, "There is no revision with following SHA-1 hash: '{}'", currentRevision);
            throw new NoSuchRevisionException("SHA-1 of revision is incorrect.");
        }
        Set<Path> removedFiles = Collections.emptySet();
        Commit deployedRevision = readCommit(mergedRevision);
        Tree deployedTree = readTree(deployedRevision.getTree());
        Map<Path, String> addedFiles = walkVcsTree(deployedTree);
        for (Map.Entry<Path, String> entry : addedFiles.entrySet()) {
            Path addedFile = entry.getKey();
            String sha = entry.getValue();
            if (!repository.isValidSha(sha)) {
                logger.error(errorMarker, "There is no object with following SHA-1 hash: '{}'", sha);
                throw new InvalidTreeException("SHA-1 hash of an object is invalid.");
            }
            Files.copy(repository.getObject(sha), repository.getDirectory().resolve(addedFile), REPLACE_EXISTING);
        }
        updateIndex(addedFiles, removedFiles);
        commit(Repository.MERGE_COMMIT_PREFIX + revisionName, mergedRevision);
    }

    /**
     * Remove repository in given directory.
     * @throws IOException Throws when something is wrong with file system.
     * @throws DirectoryExpectedException Throws when given path is not a directory.
     */
    public void removeRepository() throws
            IOException, DirectoryExpectedException{
        logger.debug("Removing repository.");
        repository.removeRepository();
    }

    /**
     * Get log of all commits, attached to current head revision.
     * @return Log message with information about commit as {@code Log} object.
     * @throws IOException Throws when something is wrong with file system.
     * @throws GitNotInitializedException Throws when there if no any repository at such path.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws DirectoryExpectedException Throws when given path is not a directory
     */
    public @Nullable Log getLog() throws IOException, GitNotInitializedException,
            DirectoryExpectedException, InvalidCommitException, HeadFileFailedException{
        logger.debug("Getting current log messages.");
        String startCommit = repository.getCurrentRevision();
        Commit headCommit = readCommit(startCommit);
        Set<String> presentSet = new HashSet<>();
        presentSet.add(startCommit);
        logger.debug("Starting dfs search to get all commits.");
        List<Commit> commits = getCommitsDFS(presentSet, new ArrayList<>(), headCommit);
        Log currentLogMessage = null;
        assert commits != null;
        for (Commit commit : commits) {
            currentLogMessage = new Log(commit, currentLogMessage);
        }
        return currentLogMessage;
    }

    /**
     * Get all commits by bfs sorted be time of creation.
     * @param presentSet SHA-1 hashes of all found commits.
     * @param commits List of all found commits.
     * @param currentCommit Commit whose parents should be examined the next moment.
     * @return List of all found commits.
     * @throws IOException Throws when something is wrong with file system.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     */
    private @Nullable List<Commit> getCommitsDFS(@Nullable Set<String> presentSet, @Nullable List<Commit> commits,
                                                 @Nullable Commit currentCommit)
            throws IOException, InvalidCommitException{
        logger.debug("Starting collecting commit list.");
        assert commits != null;
        commits.add(currentCommit);
        assert currentCommit != null;
        for (String parent : currentCommit.getParents()) {
            assert presentSet != null;
            if (!presentSet.contains(parent)) {
                presentSet.add(parent);
                getCommitsDFS(presentSet, commits, readCommit(parent));
            }
        }
        commits.sort(Comparator.comparingLong(Commit::getDateMilliseconds));
        return commits;
    }

    /**
     * Returns current repository status.
     * @return Current repository status as {@code StatusManager} object.
     * @throws IOException Throws when something is wrong with file system.
     * @throws HeadFileFailedException Throws when reading of HEAD file failed.
     * @throws InvalidCommitException Throws when commit information is incorrect.
     * @throws InvalidTreeException Throws when tree file format is incorrect.
     * @throws IndexFileFailedException Trows when there are problems with reading or writing to index file.
     * @throws NoSuchRevisionException Throws when trying to checkout to version which does not exist.
     */
    public StatusManager getRepositoryStatus() throws IOException, HeadFileFailedException, InvalidCommitException,
            NoSuchRevisionException, InvalidTreeException, IndexFileFailedException{
        logger.debug("Getting repository statistics.");
        String currentHead = repository.getCurrentHead();
        String revision;
        if (currentHead.startsWith(Repository.REFERENCE_HEAD_PREFIX)) {
            revision = currentHead.substring(Repository.REFERENCE_HEAD_PREFIX.length());
        } else {
            revision = currentHead;
        }
        String revisionSha = repository.getCurrentRevision();
        Commit commit = readCommit(revisionSha);
        Tree tree = readTree(commit.getTree());
        Map<Path, String> treeContent = walkVcsTree(tree);
        Map<Path, String> index = readIndexFile();
        StatusManager status = new StatusManager(revision);
        for (Path fileInIndex : index.keySet()) {
            if (!treeContent.containsKey(fileInIndex)) {
                status.addEntry(fileInIndex, StatusType.ADDED);
            } else if (!treeContent.get(fileInIndex).equals(index.get(fileInIndex))) {
                status.addEntry(fileInIndex, StatusType.MODIFIED);
            }
            if (!Files.exists(getRoot().resolve(fileInIndex))) {
                status.addEntry(fileInIndex, StatusType.MISSING);
            }
        }
        treeContent.keySet().stream().filter(fileInTree -> !index.containsKey(fileInTree)).forEach(fileInTree ->
                status.addEntry(fileInTree, StatusType.REMOVED));
        Set<Path> allFiles = getAllFiles();
        for (Path file : allFiles) {
            if (index.containsKey(file)) {
                Path pathToFile = getRoot().resolve(file);
                String fileSha = new Blob(Files.readAllBytes(pathToFile), pathToFile.toString()).getSha();
                if (!fileSha.equals(index.get(file))) {
                    status.addEntry(file, StatusType.UNSTAGED);
                }
            } else {
                status.addEntry(file, StatusType.UNTRACKED);
            }
        }
        return status;
    }

    /**
     * Resets given file to current revision's initial state.
     * @param path Path to file which should be removed.
     * @throws IOException Throws when something is wrong with file system.
     * @throws IndexFileFailedException Trows when there are problems with reading or writing to index file.
     */
    public void resetFile(@NotNull Path path) throws IOException, IndexFileFailedException{
        logger.debug("Resetting file: '{}'", path.toString());
        Map<Path, String> index = readIndexFile();
        path = getRoot().toAbsolutePath().relativize(path.toAbsolutePath());
        if (!index.containsKey(path)) {
            logger.error(errorMarker, "File with such a path does not exist: '{}'", path);
            throw new FileNotFoundException();
        }
        String sha = index.get(path);
        if (!repository.isValidSha(sha)) {
            logger.error(errorMarker, "There is no file with following SHA-1 hash: '{}'", sha);
            throw new IndexFileFailedException("");
        }
        Files.write(Paths.get(getRoot().toString() + "/" + path.toString()),
                Files.readAllBytes(repository.getObject(sha)));
    }

    /**
     * Removes all files that are not added to the repository.
     * @throws IOException Throws when something is wrong with file system.
     * @throws IndexFileFailedException Trows when there are problems with reading or writing to index file.
     */
    public void cleanRepository() throws IOException, IndexFileFailedException{
        logger.debug("Cleaning repository.");
        Map<Path, String> index = readIndexFile();
        Set<Path> allFiles = getAllFiles();
        for (Path file : allFiles) {
            if (!index.containsKey(file)) {
                Files.deleteIfExists(getRoot().resolve(file));
            }
        }
    }

    /**
     * Get set of paths of all files in repository.
     * @return Set of paths.
     * @throws IOException Trows if something is wrong with file system.
     */
    private Set<Path> getAllFiles() throws IOException{
        return Files.walk(getRoot())
                .filter(file -> !file.startsWith(repository.getDirectory()) && Files.isRegularFile(file))
                .map(file -> getRoot().relativize(file))
                .collect(Collectors.toSet());
    }

    /*
     * Returns parent directory of repository home directory.
     * @return Path to parent directory as {@code Path} object.
     */
    private Path getRoot() {
        return repository.getDirectory().getParent();
    }
}