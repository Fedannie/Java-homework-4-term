import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * One threaded MD5-hasher.
 */
public class MD5HasherSimple {
    /**
     * Counts and returns MD5 hash of given file.
     * @param filename path to file whose hash should be counted.
     * @return ND5 hash of file as BigInteger object.
     */
    private BigInteger mdHashOfFile(String filename) throws Exception{

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
     * Returns ND5 hash of given directory.
     *
     * @param path Path to directory whose hash should be returned.
     * @return MD5 hash of given directory as BigInteger object.
     */
    public BigInteger mdHashOfDirectory(String path) throws Exception {
        File file = new File(path);
        if (file.isFile()) {
            return mdHashOfFile(path);
        }
        MessageDigest complete = MessageDigest.getInstance("MD5");
        complete.update(file.getName().getBytes());
        BigInteger answer = new BigInteger(1, complete.digest());
        File[] content = file.listFiles();
        if (content == null) {
            return answer;
        }
        for (File temporary : content) {
            if (temporary.isDirectory()) {
                answer = answer.add(mdHashOfDirectory(temporary.getAbsolutePath()));
            } else {
                answer = answer.add(mdHashOfFile(temporary.getAbsolutePath()));
            }
        }
        return answer;
    }
}