import java.util.function.Supplier;

/**Class one-thread  implementation of lazy.
 * Result calculates once.
 *
 * @param <T> type of result.
 */
public class LazySimple<T> implements Lazy<T>{
    /**
     * Produces result.
     */
    private Supplier<T> supp = null;

    /**
     * result of supplier production
     */
    private T res = null;

    /**Creates lazySimple implementation by supplier.
     *
     * @param supplier produces result.
     */
    public LazySimple(Supplier<T> supplier) {
        supp = supplier;
    }

    /**Calculates result, if it was not calculated early. Otherwise returns the previous result.
     *
     * @return result of supplier production
     */
    @Override
    public T get() {
        if (supp != null) {
            res = supp.get();
            supp = null;
        }
        return res;
    }
}
