/**
 * Interface for lazy evaluation.
 * Evaluation starts only after first get call. Other calls will return the same object as the first one.
 *
 * @param <T> evaluation result
 */
public interface Lazy<T> {
    /**
     * Get result of evaluation.
     *
     * @return result.
     */
    T get();
}
