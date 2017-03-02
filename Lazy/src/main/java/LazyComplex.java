import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.Supplier;

/**Class lock-free-thread  implementation of lazy
 * Implementation without locks. Result may be calculated several times, but it is always the same
 *
 * @param <T> type of result
 */
public class LazyComplex<T> implements Lazy<T>{
    /**
     * produces result
     */
    private Supplier<T> supp = null;

    /**
     * result of supplier production
     */
    private final AtomicMarkableReference<T> res = new AtomicMarkableReference<>(null, false);

    /**Creates lazyComplex implementation by supplier.
     *
     * @param supplier produces result.
     */
    public LazyComplex(Supplier<T> supplier) {
        supp = supplier;
    }

    /**Calculates result without locking. Result may be calculated several times, but it is always the same.
     *
     * @return result of supplier production
     */
    @Override
    public T get() {
        if (!res.isMarked()) {
            T t = supp.get();
            res.compareAndSet(null, t, false, true);
        }
        return res.getReference();
    }
}
