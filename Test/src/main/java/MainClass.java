import java.math.BigInteger;

/**
 * Main class with main function.
 * Compares time of working of two MD5Hasher classes.
 */
public class MainClass {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Number of arguments is too small");
            return;
        }
        String directoryName = args[0];
        MD5HasherRecursiveTask recursiveHasher = new MD5HasherRecursiveTask(directoryName);
        MD5HasherSimple simpleHasher = new MD5HasherSimple();
        long timeStart = 0;
        long timeFirstFinished = 0;
        long timeBothFinished = 0;
        BigInteger first = BigInteger.valueOf(0);
        BigInteger second = BigInteger.valueOf(0);
        try {
            timeStart = System.currentTimeMillis();
            first = simpleHasher.mdHashOfDirectory(directoryName);
            timeFirstFinished = System.currentTimeMillis();
            second = recursiveHasher.compute();
            timeBothFinished = System.currentTimeMillis();
        } catch (Exception e){
            System.out.println("OOps! Something went wrong.");
        }
        System.out.println("It's OK!");
        System.out.println("Simple hasher worked : " + (new Long(timeBothFinished - timeFirstFinished)).toString() +
                "    and returned : " + second.toString());
        System.out.println("Fork/Join hasher worked : " + (new Long(timeFirstFinished - timeStart)).toString() +
                "    and returned : " + first.toString());
    }
}
