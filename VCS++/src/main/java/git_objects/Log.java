package git_objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;

/**Log message which gives information about one commit.*/
public class Log {
    /**The commit this log message provides information about.*/
    private @NotNull Commit commit;

    /** Next log message.*/
    private @Nullable Log nextLogMessage;

    /**
     * Public constructor of {@code Log} object.
     * @param commit Commit which new log message should give information about.
     * @param nextLogMessage Next log message as {@code Log} object.
     */
    public Log(@NotNull Commit commit, @Nullable Log nextLogMessage) {
        this.commit = commit;
        this.nextLogMessage = nextLogMessage;
    }

    /**
     * Gives next log message.
     * @return Next log message as {@code Log} object.
     */
    public @Nullable Log getNextLogMessage() {
        return nextLogMessage;
    }

    /**
     * Gives information about commit as {@code String} object.
     * @return Information about commit as {@code String}.
     */
    public @NotNull String getMessage() {
        return "commit hash: " + commit.getSha() + '\n' +
                "author: " + commit.getAuthor() + '\n' +
                "date: " + DateFormat.getDateTimeInstance().format(commit.getDate()) + '\n' +
                "message: " + commit.getMessage();
    }
}
