import java.util.function.Supplier;

/** Class that creates lazy implementations*/
public class LazyFactory {
    /** Creates simple one-thread lazy implementation.
     *
     * @param supplier produces result.
     * @param <T> type of result.
     * @return lazy one-thread implementation.
     */
    public static <T> Lazy<T> createLazySimple(Supplier<T> supplier) {
        return new LazySimple<T>(supplier);
    }

    /**Creates simple multi-thread lazy implementation.
     *
     * @param supplier produces result.
     * @param <T> type of result.
     * @return lazy multi-thread implementation.
     */
    public static <T> Lazy<T> createLazyThread(Supplier<T> supplier) {
        return new LazyThread<T>(supplier);
    }

    /**Creates simple lock-free-thread lazy implementation.
     *
     * @param supplier produces result.
     * @param <T> type of result.
     * @return lazy lock-free-thread implementation.
     */
    public static <T> Lazy<T> createLazyComplex(Supplier<T> supplier) {
        return new LazyComplex<T>(supplier);
    }
}
