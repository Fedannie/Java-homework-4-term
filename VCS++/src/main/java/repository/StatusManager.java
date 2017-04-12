package repository;

import git_objects.StatusEntry;
import git_objects.StatusType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Repository statistics manager.
 * This statistic contains information about current file state and how repository treats this state.
 */
public class StatusManager {
    /**Current revision.*/
    private @NotNull String revision;
    /**All entries attached to current revision.*/
    private @NotNull Set<StatusEntry> entries;

    /**
     * Creates {@code StatusManager} by revision.
     * @param revision Current revision.
     */
    public StatusManager(@NotNull String revision) {
        this.revision = revision;
        entries = new HashSet<>();
    }

    /**
     * Returns current revision.
     * @return Current revision as {@code String} object.
     */
    public @NotNull String getRevision() {
        return revision;
    }

    /**
     * Returns all status entries attached to current revision.
     * @return Entries as {@code Set<StatusEntry>} object.
     */
    public @NotNull Set<StatusEntry> getEntries() {
        return entries;
    }

    /**
     * Add entry to set.
     * @param entry Entry which should be added to all entries attached to current revision.
     */
    public void addEntry(StatusEntry entry) {
        entries.add(entry);
    }

    /**
     * Create new status entry and add it to all entries attached to current revision.
     * @param path Path to file status of which should be created.
     * @param type Type of status of given file.
     */
    public void addEntry(@NotNull Path path, @NotNull StatusType type) {
        addEntry(new StatusEntry(path, type));
    }
}
