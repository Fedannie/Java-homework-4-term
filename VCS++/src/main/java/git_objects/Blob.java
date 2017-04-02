package git_objects;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.sun.istack.internal.NotNull;

import java.io.Serializable;

/**
 * Class of files, store content of file.
 */
public class Blob extends GitObjectNamed{
    /** Content of blob file.*/
    @NotNull
    private final byte[] content;

    /**
     * Public constructor. Saves given content.
     * @param content Content of new blob file.
     * @param name Name of new blob object.
     */
    public Blob(@NotNull byte[] content, @NotNull String name) {
        super(name, GitObjectType.BLOB);
        this.content = content;
    }

    /**
     * Returns content of this blob file.
     * @return Content as byte array.
     */
    @Override
    public byte[] getContent() {
        return content;
    }
}
