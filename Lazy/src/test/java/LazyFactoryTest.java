import org.junit.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.function.Supplier;

import static org.junit.Assert.*;


public class LazyFactoryTest {

    private static final int THREAD_CNT = 5;

    private class Counted implements Supplier<Integer> {
        private int count = 0;

        int getCount() {
            return count;
        }

        public Integer get() {
            count++;
            return count;
        }
    }

    private class CountedSupplier<T> implements Supplier<T> {

        private int count = 0;
        private final Supplier<T> sup;

        CountedSupplier(Supplier<T> sup) {
            this.sup = sup;
        }

        int getCount() {
            return  count;
        }

        public T get() {
            count++;
            return sup.get();
        }
    }

    private final Supplier<String> nullSupplier = new Supplier<String>() {
        private boolean isCalled = false;

        @Override
        public String get() {
            assertFalse(isCalled);
            isCalled = true;
            return null;
        }
    };

    private void checkForNullTests(Lazy<String> lazy) {
        assertNull(lazy.get());
        assertNull(lazy.get());
    }

    private void checkForEqualTests(Lazy<String> lazy) {
        String first = lazy.get();
        String second = lazy.get();
        assertEquals(first, second);
    }

    private void checkMultiThreadLazy (Lazy<String> lazy, String expected) {
        CyclicBarrier barrier = new CyclicBarrier(THREAD_CNT);
        Thread[] threads = new Thread[THREAD_CNT];

        Object[] answers = new Object[THREAD_CNT];
        for (int i = 0; i < THREAD_CNT; i++) {
            final int t = i;
            threads[t] = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                answers[t] = lazy.get();

                assertEquals(expected, answers[t]);
                assertSame(answers[t], lazy.get());
            });
            threads[t].start();
        }
        for (int i = 0; i < THREAD_CNT; i++) {
            try {
                threads[i].join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = 1; i < THREAD_CNT; i++) {
            assertSame(answers[i - 1], answers[i]);
        }
    }

    @Test
    public void createLazySimpleTest() throws Exception {
        final Counted supplier = new Counted();
        assertEquals(0, supplier.getCount());

        final Lazy<Integer> lazy = LazyFactory.createLazySimple(supplier);

        final Integer a = lazy.get();
        final Integer b = lazy.get();

        assertEquals((Integer) 1, a);
        assertEquals(1, supplier.getCount());

        assertEquals((Integer) 1, lazy.get());
        assertSame(a, b);
    }

    @Test
    public void createLazyThreadTest() throws Exception {
        CountedSupplier<String> countedSupplier = new CountedSupplier<>(() -> "abc");
        Lazy<String> lazy = LazyFactory.createLazyThread(countedSupplier);
        String expected = "abc";
        assertEquals(0, countedSupplier.getCount());
        checkMultiThreadLazy(lazy, expected);
    }

    @Test
    public void createLazyComplexTest() throws Exception {
        CountedSupplier<String> countedSupplier = new CountedSupplier<>(() -> "abc");
        Lazy<String> lazy = LazyFactory.createLazyComplex(countedSupplier);
        String expected = "abc";
        assertEquals(0, countedSupplier.getCount());
        checkMultiThreadLazy(lazy, expected);
    }

    @Test
    public void createLazySimpleNullTest() throws Exception {
        Lazy<String> test = LazyFactory.createLazySimple(nullSupplier);
        checkForNullTests(test);
    }

    @Test
    public void createLazyThreadNullTest() throws Exception {
        Lazy<String> lazy = LazyFactory.createLazyThread(nullSupplier);
        checkMultiThreadLazy(lazy, null);
    }

    @Test
    public void createLazyComplexNullTest() throws Exception {
        Lazy<String> lazy = LazyFactory.createLazyComplex(nullSupplier);
        checkMultiThreadLazy(lazy, null);
    }

    @Test
    public void createLazySimpleEqualTest() throws Exception {
        Lazy<String> test = LazyFactory.createLazySimple(nullSupplier);
        checkForEqualTests(test);
    }
}