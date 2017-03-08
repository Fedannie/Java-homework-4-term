import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**Class lock-free-thread  implementation of lazy.
 * Implementation without locks. Result may be calculated several times, but it is always the same.
 *
 * @param <T> type of result
 */
public class LazyComplex<T> implements Lazy<T>{
    /** Produces result. */
    private Supplier<T> supplier = null;

    /** Result of supplier production. */
    private volatile T result = null;

    /** Atomic updater of field 'result' */
    private static final AtomicReferenceFieldUpdater<LazyComplex, Object> updater =
                         AtomicReferenceFieldUpdater.newUpdater(LazyComplex.class, Object.class, "result");

    /**Creates lazyComplex implementation by supplier.
     *
     * @param supplier produces result.
     */
    public LazyComplex(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**Calculates result without locking. Result may be calculated several times, but it is always the same.
     *
     * @return result of supplier production.
     */
    @Override
    public T get() {
        if (supplier != null) {
            updater.compareAndSet(this, null, supplier.get());
            supplier = null;
        }
        return result;
    }
}
