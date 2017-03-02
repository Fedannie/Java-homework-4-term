import java.util.function.Supplier;

/**Class multi-thread  implementation of lazy.
 * Result calculates once.
 *
 * @param <T> type of result.
 */
public class LazyThread<T> implements Lazy<T> {
    /**
     * Produces result.
     */
    private Supplier<T> supp = null;

    /**
     * Result of supplier production.
     */
    private volatile T res = null;

    /**Creates lazyThread implementation by supplier.
     *
     * @param supplier produces result.
     */
    public LazyThread(Supplier<T> supplier) {
        supp = supplier;
    }

    /**Calculates result with locking, if it was not calculated early. Otherwise returns the previous result.
     *
     * @return result of supplier production
     */
    @Override
    public T get() {
        Supplier<T> tmp = supp;
        if (tmp != null) {
            synchronized (this) {
                tmp = supp;
                if (tmp != null) {
                    res = supp.get();
                    supp = null;
                }
            }
        }
        return res;
    }
}
