import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * MD5HasherRecursiveTask class. Evaluates MD5 hash of directory with fork/join pool.
 */
public class MD5HasherRecursiveTask extends RecursiveTask<BigInteger>{
    /**Name of file which MD5 hash shoulb be computed */
    private File file;

    /**
     * Public constructor.
     * Saves file.
     * @param path Path to file/directory VD5 hash of which should be calculated.
     */
    public MD5HasherRecursiveTask(String path){
        this.file = new File(path);
    }

    /**
     * Counts and returns MD5 hash of given file.
     * @param filename path to file whose hash should be counted.
     * @return ND5 hash of file as BigInteger object.
     */
    public BigInteger mdHashOfFile(String filename) throws Exception{

        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return new BigInteger(1, complete.digest());
    }

    /**
     * Overwritten method to perform fork/join calculations.
     * @return BigInteger as MD5 hash of file given in constructor.
     */
    @Override
    protected BigInteger compute() throws RuntimeException{
        MessageDigest complete;
        BigInteger result;
        List<MD5HasherRecursiveTask> subTasks = new LinkedList<>();
        try {
            complete = MessageDigest.getInstance("MD5");
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        if (file.isDirectory()){
            complete.update(file.getName().getBytes());
            result = new BigInteger(1, complete.digest());
            for (File child : file.listFiles()){
                MD5HasherRecursiveTask task = new MD5HasherRecursiveTask(child.getAbsolutePath());
                task.fork();
                subTasks.add(task);
            }

            for (MD5HasherRecursiveTask task : subTasks){
                result = result.add(task.join());
            }
        } else {
            try{
                result = mdHashOfFile(file.getAbsolutePath());
            } catch (Exception e){
                throw new RuntimeException(e.getMessage());
            }
        }
        return result;
    }
}
