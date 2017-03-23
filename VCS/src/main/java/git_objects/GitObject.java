package git_objects;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.sun.istack.internal.NotNull;

import java.io.Serializable;

/**Parent of all objects in repository.*/
public abstract class GitObject implements Serializable, Comparable{
    /**Content of the file as array of bytes.*/
    public abstract byte[] getContent();
    /**SHA-1 hash of GitObject*/
    private String sha = "";
    /**
     * {@code true} if SHA-1 hash is given to constructor.
     * {@code false} if SHA-1 hash was not given to constructor.
     */
    private boolean definite = false;

    /**Type of object in MyGit repository. */
    @NotNull
    private final GitObjectType type;

    /**
     * Public constructor. Creates GitObject of given type.
     * Sha hash is null.
     * @param type Type of created object.
     */
    public GitObject(@NotNull GitObjectType type){
        this.type = type;
    }

    /**
     * Public constructor. Creates GitObject of given type
     * with SHA-1 hash as given.
     * @param type Type of created object.
     * @param sha Sha hash of an object.
     */
    public GitObject(@NotNull GitObjectType type, @NotNull String sha){
        this.type = type;
        this.sha = sha;
        definite = true;
    }

    /**Returns SHA-1 hash of an object.
     * @return Hash of object as String.
     */
    public String getSha() {
        if (definite) {
            return this.sha;
        }
        HashFunction sha1HashFunction = Hashing.sha1();
        return sha1HashFunction.newHasher().putBytes(getContent()).hash().toString();
    }

    /**Returns type of an object.
     * @return Type of object as GitObjectType object.
     */
    public GitObjectType getType() {
        return type;
    }
}
