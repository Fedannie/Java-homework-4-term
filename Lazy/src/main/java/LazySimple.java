import java.util.function.Supplier;

/**Class one-thread  implementation of lazy.
 * Result calculates once.
 *
 * @param <T> type of result.
 */
public class LazySimple<T> implements Lazy<T>{
    /** Produces result. */
    private Supplier<T> supplier = null;

    /** Result of supplier production. */
    private T result = null;

    /**Creates lazySimple implementation by supplier.
     *
     * @param supplier produces result.
     */
    public LazySimple(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**Calculates result, if it was not calculated early. Otherwise returns the previous result.
     *
     * @return result of supplier production
     */
    @Override
    public T get() {
        if (supplier != null) {
            result = supplier.get();
            supplier = null;
        }
        return result;
    }
}
