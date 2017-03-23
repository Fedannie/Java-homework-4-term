package git_objects;

import com.google.common.hash.HashFunction;

import com.google.common.hash.Hashing;
import com.sun.istack.internal.NotNull;

/**GitObjects which have names. */
public class GitObjectNamed extends GitObject{
    /**Name of GitObject.*/
    @NotNull
    private final String name;

    /**
     * Returns name of an object.
     * @return Name of object as string.
     */
    public String getName() {
        return name;
    }

    /**
     * Public constructor of an object by its name and type.
     * @param name Name of created object.
     * @param type Type of created object.
     */
    public GitObjectNamed(@NotNull String name, @NotNull GitObjectType type) {
        super(type);
        this.name = name;
    }

    /**
     * Public constructor of an object by its name, type and SHA-1 hash.
     * @param name Name of created object.
     * @param type Type of created object.
     * @param sha SHA-1 hash of created object.
     */
    public GitObjectNamed(@NotNull String name, @NotNull GitObjectType type, @NotNull String sha) {
        super(type, sha);
        this.name = name;
    }

    /**
     * Overwritten function of comparison by name.
     * @param object GitObjectNamed to compare current with.
     * @return Return {@code int} as result of comparison.
     */
    @Override
    public int compareTo(Object object) {
        return name.compareTo(((GitObjectNamed) object).getName());
    }

    /**
     * Get content of named object.
     * @return Content of object as array of bytes.
     */
    @Override
    public byte[] getContent() {
        return new byte[0];
    }
}
