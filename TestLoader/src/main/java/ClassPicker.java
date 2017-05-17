import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**Class that searches for and collects class files.*/
public class ClassPicker extends SimpleFileVisitor<Path>{
    /**Extension of collected files.*/
    private static final @NotNull String extension = "java";
    /**Path to root directory where classes should be found.*/
    private final @NotNull Path root;
    /**List of all test classes.*/
    private final @NotNull List<Class<?>> testClasses;

    /**
     * Public constructor. Creates {@code ClassPicker} object by root directory.
     * @param root -- root directory as {@code Path} object.
     */
    public ClassPicker(@NotNull Path root) {
        this.root = root;
        this.testClasses = new ArrayList<>();
    }

    /**
     * Get list of collected test classes.
     * @return all test classes as {@code List<Class>} object.
     */
    @NotNull
    public List<Class<?>> getTestClasses() {
        return new ArrayList<>(testClasses);
    }

    /**
     * Collects file with test class if it has a suitable extension.
     * @param path -- path to class
     * @param attrs -- attributes of parent class which are not used in current method
     * @return result of execution. FileVisitResult.CONTINUE if all went OK and trows exception otherwise.
     */
    @Override
    public FileVisitResult visitFile(@NotNull Path path, @Nullable BasicFileAttributes attrs){
        if (FilenameUtils.getExtension(path.toString()).equals(extension)){
            try {
                path = root.relativize(path);
                StringBuilder className = new StringBuilder();
                for (Path name : path.getParent()) {
                    className.append(name.toString());
                    className.append(".");
                }
                className.append(FilenameUtils.getBaseName(path.getFileName().toString()));
                testClasses.add(Class.forName(className.toString()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return FileVisitResult.CONTINUE;
    }
}
