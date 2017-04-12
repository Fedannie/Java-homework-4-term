package git_objects;

import org.jetbrains.annotations.NotNull;
import repository.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**Class that store information about commits.*/
public class Commit extends GitObject{
    /**Message which belong to this commit.*/
    @NotNull
    private final String message;

    /**Author of this commit.*/
    @NotNull
    private final String author;

    /**Date of creation of this commit.*/
    @NotNull
    private final Date date;

    /**Hash of base tree object.*/
    @NotNull
    private final String tree;

    /**Hashes of parent commits.*/
    @NotNull
    private final List<String> parents;

    /**
     * Public constructor of commit.
     * @param tree Hash of base tree.
     * @param message Message with which commit is created.
     * @param parents Hashes of parent commits.
     * @param author Name of creator of commit.
     * @param date Date of creation of commit.
     */
    public Commit(@NotNull String tree, @NotNull String message,
                  @NotNull List<String> parents, @NotNull String author,
                  @NotNull Date date) {
        super(GitObjectType.COMMIT);
        this.tree = tree;
        this.message = message;
        this.parents = parents;
        this.author = author;
        this.date = date;
    }

    /**
     * Returns message with which commit was created.
     * @return Message as string.
     */
    @NotNull
    public String getMessage() {
        return message;
    }

    /**
     * Returns name of creator of this commit.
     * @return Name of author as string.
     */
    @NotNull String getAuthor() {
        return author;
    }

    /**
     * Returns date of creationf of this commit.
     * @return Date as Date type.
     */
    @NotNull Date getDate() {
        return date;
    }

    /**
     * Returns date of creationf of this commit.
     * @return Date as Date type.
     */
    public long getDateMilliseconds() {
        return date.getTime();
    }

    /**
     * Retruns hash of base tree.
     * @return Hash of base tree as string.
     */
    @NotNull
    public String getTree() {
        return tree;
    }

    /**
     * Returns hashes of parents of this commit.
     * @return Hashes of parents as list of string.
     */
    @NotNull
    public List<String> getParents() {
        return parents;
    }

    /**
     * Add given string to {@code parents} array of this commit.
     * @param parent Given SHA-1 hash of new parent.
     */
    public void addParent(String parent) {
        parents.add(parent);
    }

    /**
     * Returns information about commit: parents, tree, author and date.
     * @return Information about commit as String.
     */
    @Override
    public String toString() {
        String parentsString = getParents().stream()
                .map(parent -> Repository.PARENT_COMMIT_PREFIX + parent)
                .collect(Collectors.joining("\n"));
        return getTree() + '\n' + getAuthor() + '\n' + date.getTime() +
                (parentsString.isEmpty() ? "" : '\n' + parentsString) + '\n' +
                Repository.MESSAGE_COMMIT_PREFIX + getMessage();
    }

    /**
     * Returns content of commit as array of bytes.
     * @return Array of bytes with information.
     */
    @Override
    public byte[] getContent() {
        return toString().getBytes();
    }

    /**
     * Overwritten function of comparison by date.
     * @param object Date to compare current with.
     * @return Return {@code int} as result of comparison.
     */
    @Override
    public int compareTo(@NotNull Object object) {
        return date.compareTo(((Commit) object).getDate());
    }
}
