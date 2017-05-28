package git_objects;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Class of directory in repository, store blobs and subdirectories.
 */
public class Tree extends GitObjectNamed{
    /**Everything which is located in this directory.*/
    @NotNull
    private Set<GitObjectNamed> children;

    /**
     * Returns everything located in this directory.
     * @return All files as list of TreeChild objects.
     */
    @NotNull
    public Set<GitObjectNamed> getChildren() {
        return children;
    }

    /**
     * Public constructor of empty tree.
     * @param name Name of new branch.
     */
    public Tree(@NotNull String name) {
        super(name, GitObjectType.TREE);
        this.children = new TreeSet<>();
    }

    /**
     * Content of {@code Tree} object.
     * @return Content as array of bytes.
     */
    @Override
    public byte[] getContent() {
        return (children.stream().map(child -> child.getType().toString() + "\t" +
                child.getSha() + "\t" + child.getName()).collect(Collectors.joining("\n"))).getBytes();
    }

    /**
     * Add a child to a tree.
     * @param object Child wanted to be add.
     */
    public boolean addChild(@NotNull GitObjectNamed object) {
        return children.add(object);
    }
}
