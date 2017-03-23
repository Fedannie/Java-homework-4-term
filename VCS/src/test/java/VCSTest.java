import static org.junit.Assert.*;
import static repository.Manager.*;

import exceptions.CanNotDeleteBranchException;
import git_objects.GitConstants;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import repository.Manager;
import repository.Repository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class VCSTest {
    private Path repositoryPath = Paths.get("JavaTest");
    private Path file1 = new File(repositoryPath.toString() + "/file1").toPath();
    private Repository repository;
    @Before
    public void initRepository() throws Exception {
        Files.createDirectory(repositoryPath);
        repository = init(repositoryPath);
    }

    @Test
    public void getRepositoryTest() throws Exception {
        findRepository(repositoryPath);
    }

    @Test
    public void simpleAddTest() throws Exception {
        Files.createDirectory(repositoryPath.resolve("dir1"));
        Files.createFile(repositoryPath.resolve("dir1/file1"));
        Files.createFile(repositoryPath.resolve("dir1/file2"));

        addFile(repository, repositoryPath.resolve("dir1/file1"));
        addFile(repository, repositoryPath.resolve("dir1/file2"));

        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));
    }

    @Test
    public void simpleCheckoutTest() throws Exception {
        byte[] content1 = "testContent1".getBytes();
        Files.write(file1, content1);
        addToIndex(file1);
        commit(repository, "test commit message", null);
        newBranch(repository, "branch");

        Path file2 = new File(repositoryPath.toString() + "/file2").toPath();
        byte[] content2 = "testContent2".getBytes();
        Files.write(file2, content2);
        addToIndex(file2);
        commit(repository, "another test commit message", null);

        checkout(repository, GitConstants.DEFAULT_BRANCH_NAME);
        byte[] content3 = "testContent3".getBytes();
        Files.write(file2, content3);
        addToIndex(file2);
        commit(repository, "another test commit message", null);
        checkout(repository, "branch");
        assertEquals("branch", currentBranch(repository));
    }

    @Test
    public void simpleWorkWithBranchesTest() throws Exception {
        assertEquals("master", currentBranch(repository));
        Path file1 = new File(repositoryPath.toString() + "/file1").toPath();
        byte[] content1 = "testContent1".getBytes();
        Files.write(file1, content1);
        addToIndex(file1);
        commit(repository, "test commit message", null);
        newBranch(repository, "super_branch");
        assertEquals("super_branch", currentBranch(repository));
        deleteBranch(repository, "master");
        assertEquals("super_branch", currentBranch(repository));
    }


    @Test
    public void deleteBranchTest() throws Exception{
        assertEquals("master", currentBranch(repository));
        Path file1 = new File(repositoryPath.toString() + "/file1").toPath();
        byte[] content1 = "testContent1".getBytes();
        Files.write(file1, content1);
        addToIndex(file1);
        commit(repository, "test commit message", null);
        newBranch(repository, "super_branch");
        assertEquals("super_branch", currentBranch(repository));
        newBranch(repository, "super_puper_branch");
        assertEquals("super_puper_branch", currentBranch(repository));
        deleteBranch(repository, "super_branch");
        assertEquals("super_puper_branch", currentBranch(repository));
        checkout(repository, "master");
        assertEquals("master", currentBranch(repository));
        deleteBranch(repository, "super_puper_branch");
        assertEquals("master", currentBranch(repository));
    }

    @Test
    public void simpleMergeTest() throws Exception {
        Path file1 = new File(repositoryPath.toString() + "/file1").toPath();
        byte[] content1 = "testContent1".getBytes();
        Files.write(file1, content1);
        addToIndex(file1);
        commit(repository, "test commit message", null);
        newBranch(repository, "first");
        commit(repository, "first-1", null);
        checkout(repository, "master");
        assertEquals("master", currentBranch(repository));
        merge(repository, "first");
    }

    @Test
    public void mergeTest() throws Exception {
        commit(repository, "Initial commit.", null);
        newBranch(repository, "first");

        Files.createDirectory(repositoryPath.resolve("dir1"));
        Files.createFile(repositoryPath.resolve("dir1/file1"));

        addFile(repository, repositoryPath.resolve("dir1/file1"));
        commit(repository, "First commit", null);
        checkout(repository, "master");
        newBranch(repository, "second");

        Files.createFile(repositoryPath.resolve("dir1/file2"));

        addFile(repository, repositoryPath.resolve("dir1/file2"));
        commit(repository, "Second commit", null);

        checkout(repository, "master");

        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));

        merge(repository, "first");

        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));

        newBranch(repository, "third");

        Files.createDirectory(repositoryPath.resolve("dir2"));
        Files.createFile(repositoryPath.resolve("dir2/file1"));

        addFile(repository, repositoryPath.resolve("dir2/file1"));
        commit(repository, "Third commit", null);
        merge(repository, "second");

        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));
        assertTrue(Files.exists(repositoryPath.resolve("dir2/file1")));

        checkout(repository, "master");
        merge(repository, "second");

        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));
        assertTrue(Files.exists(repositoryPath.resolve("dir2/file1")));

        merge(repository, "third");
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));
        assertTrue(Files.exists(repositoryPath.resolve("dir2/file1")));
    }

    @Test(expected = CanNotDeleteBranchException.class)
    public void allBranchesTest() throws Exception{
        commit(repository, "Initial commit.", null);
        newBranch(repository, "first");
        newBranch(repository, "second");
        newBranch(repository, "third");
        System.out.println(allBranches(repository));
        deleteBranch(repository, "second");
        System.out.println(allBranches(repository));
        deleteBranch(repository, "master");
        System.out.println(allBranches(repository));
        deleteBranch(repository, "third");
        System.out.println(allBranches(repository));
        System.out.println(currentBranch(repository));
    }

    @After
    public void removeRepositoryTest() throws Exception {
        removeRepository(repository);
        FileUtils.deleteDirectory(repositoryPath.toFile());
    }
}
