import static org.junit.Assert.*;

import exceptions.CanNotDeleteBranchException;
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

public class VCSTest {
    private Path repositoryPath = Paths.get("JavaTest");
    private Path file1 = new File(repositoryPath.toString() + "/file1").toPath();
    private Manager manager;
    @Before
    public void initRepository() throws Exception {
        Files.createDirectory(repositoryPath);
        Manager.init(repositoryPath);
        manager = new Manager(repositoryPath);
    }
    
    @Test
    public void simpleAddTest() throws Exception {
        Files.createDirectory(repositoryPath.resolve("dir1"));
        Files.createFile(repositoryPath.resolve("dir1/file1"));
        Files.createFile(repositoryPath.resolve("dir1/file2"));

        manager.addFile(repositoryPath.resolve("dir1/file1"));
        manager.addFile(repositoryPath.resolve("dir1/file2"));

        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));
    }

    @Test
    public void simpleCheckoutTest() throws Exception {
        byte[] content1 = "testContent1".getBytes();
        Files.write(file1, content1);
        manager.addToIndex(file1);
        manager.commit("test commit message", null);
        manager.newBranch("branch");

        Path file2 = new File(repositoryPath.toString() + "/file2").toPath();
        byte[] content2 = "testContent2".getBytes();
        Files.write(file2, content2);
        manager.addToIndex(file2);
        manager.commit("another test commit message", null);

        manager.checkout(Repository.DEFAULT_BRANCH_NAME);
        byte[] content3 = "testContent3".getBytes();
        Files.write(file2, content3);
        manager.addToIndex(file2);
        manager.commit("another test commit message", null);
        manager.checkout("branch");
        assertEquals("branch", manager.currentBranch());
    }

    @Test
    public void simpleWorkWithBranchesTest() throws Exception {
        assertEquals("master", manager.currentBranch());
        Path file1 = new File(repositoryPath.toString() + "/file1").toPath();
        byte[] content1 = "testContent1".getBytes();
        Files.write(file1, content1);
        manager.addToIndex(file1);
        manager.commit("test commit message", null);
        manager.newBranch("super_branch");
        assertEquals("super_branch", manager.currentBranch());
        manager.deleteBranch("master");
        assertEquals("super_branch", manager.currentBranch());
    }


    @Test
    public void deleteBranchTest() throws Exception{
        assertEquals("master", manager.currentBranch());
        Path file1 = new File(repositoryPath.toString() + "/file1").toPath();
        byte[] content1 = "testContent1".getBytes();
        Files.write(file1, content1);
        manager.addToIndex(file1);
        manager.commit("test commit message", null);
        manager.newBranch("super_branch");
        assertEquals("super_branch", manager.currentBranch());
        manager.newBranch("super_puper_branch");
        assertEquals("super_puper_branch", manager.currentBranch());
        manager.deleteBranch("super_branch");
        assertEquals("super_puper_branch", manager.currentBranch());
        manager.checkout("master");
        assertEquals("master", manager.currentBranch());
        manager.deleteBranch("super_puper_branch");
        assertEquals("master", manager.currentBranch());
    }

    @Test
    public void simpleMergeTest() throws Exception {
        Path file1 = new File(repositoryPath.toString() + "/file1").toPath();
        byte[] content1 = "testContent1".getBytes();
        Files.write(file1, content1);
        manager.addToIndex(file1);
        manager.commit("test commit message", null);
        manager.newBranch("first");
        manager.commit("first-1", null);
        manager.checkout("master");
        assertEquals("master", manager.currentBranch());
        manager.merge("first");
    }

    @Test
    public void mergeTest() throws Exception {
        manager.commit("Initial commit.", null);
        manager.newBranch("first");

        Files.createDirectory(repositoryPath.resolve("dir1"));
        Files.createFile(repositoryPath.resolve("dir1/file1"));

        manager.addFile(repositoryPath.resolve("dir1/file1"));
        manager.commit("First commit", null);
        manager.checkout("master");
        manager.newBranch("second");

        Files.createFile(repositoryPath.resolve("dir1/file2"));

        manager.addFile(repositoryPath.resolve("dir1/file2"));
        manager.commit("Second commit", null);

        manager.checkout("master");

        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));

        manager.merge("first");

        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));

        manager.newBranch("third");

        Files.createDirectory(repositoryPath.resolve("dir2"));
        Files.createFile(repositoryPath.resolve("dir2/file1"));

        manager.addFile(repositoryPath.resolve("dir2/file1"));
        manager.commit("Third commit", null);
        manager.merge("second");

        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));
        assertTrue(Files.exists(repositoryPath.resolve("dir2/file1")));

        manager.checkout("master");
        manager.merge("second");

        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));
        assertTrue(Files.exists(repositoryPath.resolve("dir2/file1")));

        manager.merge("third");
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file1")));
        assertTrue(Files.exists(repositoryPath.resolve("dir1/file2")));
        assertTrue(Files.exists(repositoryPath.resolve("dir2/file1")));
    }

    @Test(expected = CanNotDeleteBranchException.class)
    public void allBranchesTest() throws Exception{
        manager.commit("Initial commit.", null);
        manager.newBranch("first");
        manager.newBranch("second");
        manager.newBranch("third");
        System.out.println(manager.allBranches());
        manager.deleteBranch("second");
        System.out.println(manager.allBranches());
        manager.deleteBranch("master");
        System.out.println(manager.allBranches());
        manager.deleteBranch("third");
        System.out.println(manager.allBranches());
        System.out.println(manager.currentBranch());
    }

    @After
    public void removeRepositoryTest() throws Exception {
        manager.removeRepository();
        FileUtils.deleteDirectory(repositoryPath.toFile());
    }
}
