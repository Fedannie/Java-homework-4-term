package TestClasses;

import annotations.After;
import annotations.Before;
import annotations.Test;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class BeforeAfterTest {
    @Before
    public void before() {
        TestingHelper.LIST.add("before");
    }

    @Test
    public void test() {
        TestingHelper.LIST.add("test");
    }

    @After
    public void after() {
        TestingHelper.LIST.add("after");
    }

    public static class TestingHelper extends Exception {
        public static @NotNull List<String> LIST = new LinkedList<>();
    }
}
