import java.util.function.Supplier;

/**Class multi-thread  implementation of lazy.
 * Result calculates once.
 *
 * @param <T> type of result.
 */
public class LazyThread<T> implements Lazy<T> {
    /** Produces result. */
    private Supplier<T> supplier = null;

    /** Result of supplier production. */
    private volatile T result = null;

    /**Creates lazyThread implementation by supplier.
     *
     * @param supplier produces result.
     */
    public LazyThread(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**Calculates result with locking, if it was not calculated early. Otherwise returns the previous result.
     *
     * @return result of supplier production.
     */
    @Override
    public T get() {
        Supplier<T> tmp = supplier;
        if (tmp != null) {
            synchronized (this) {
                tmp = supplier;
                if (tmp != null) {
                    result = supplier.get();
                    supplier = null;
                }
            }
        }
        return result;
    }
}
