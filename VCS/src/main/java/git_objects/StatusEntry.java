package git_objects;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Each {@code StatusEntry} contains status information of specified file.
 */
public class StatusEntry {
    /**Path to current file.*/
    private @NotNull Path path;
    /**Status of current file.*/
    private @NotNull StatusType type;

    /**
     * Public constructor, creates {@code StatusEntry} object of current file, given be path.
     * @param path Path to current file which status should be tracked.
     * @param type Current status of given file.
     */
    public StatusEntry(@NotNull Path path, @NotNull StatusType type) {
        this.path = path;
        this.type = type;
    }

    /**
     * Returns path to current file.
     * @return Path to file as {@code Path} object.
     */
    @NotNull
    public Path getPath() {
        return path;
    }


    /**
     * Returns status of current file.
     * @return Current status of file as {@code StatusType} object.
     */
    @NotNull
    public StatusType getType() {
        return type;
    }

    /**
     * Compares to {@code StatusEntry} objects by path to file and type of entry.
     * @param o Second object for comparison.
     * @return {@code true} if objects have the same paths and types, or {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatusEntry that = (StatusEntry) o;

        return path.equals(that.path) && type == that.type;
    }

    /**
     * Creates hash code considering only path to file and type of status.
     * @return Hash code of object as an integer number.
     */
    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
