package git_objects;

/**Class which keeps information about constants of MyGit.*/
public class GitConstants {
    /**Name of folder in which every repository should be located.*/
    public static final String REPOSITORY_DIRECTORY_NAME = ".vcs";
    /**Name of folder {@code objects} in MyGit repository.*/
    public static final String OBJECTS_FOLDER_NAME = "objects";
    /**Name of folder {@code references} in MyGit repository.*/
    public static final String REFERENCES_FOLDER_NAME = "refs";
    /**Name of {@code HEAD} file in MyGit repository.*/
    public static final String HEAD_FILE_NAME = "HEAD";
    /**Name of {@code index} file in MyGit repository.*/
    public static final String INDEX_FILE_NAME = "index";

    ///**Name of folder {@code branches} in MyGit repository.*/
    //public static final String BRANCHES_FOLDER_NAME = "branches";

    /**Name of default branch name in MyGit repository.*/
    public static final String DEFAULT_BRANCH_NAME = "master";
    /**Name of username property in MyGit repository.*/
    public static final String USER_NAME_PROPERTY = "user.name";
    /**Length of name of {@code objects} folder in MyGit repository.*/
    public static final int OBJECTS_FOLDER_NAME_LENGTH = 2;
    /**Prefix that is added to HEAD reference.*/
    public static final String REFERENCE_HEAD_PREFIX = "ref: ";
    /**Prefix that is added to name of parent of commit.*/
    public static final String PARENT_COMMIT_PREFIX = "parent: ";
    /**Prefix that is added to commit message.*/
    public static final String MESSAGE_COMMIT_PREFIX = "message: ";
    /**Prefix tha is added to commit message if it was merged.*/
    public static final String MERGE_COMMIT_PREFIX = "merge: ";
}