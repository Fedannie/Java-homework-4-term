package TestClasses;

import annotations.AfterClass;
import annotations.BeforeClass;
import annotations.Test;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class BeforeAfterClassTest {
    @BeforeClass
    public static void beforeClass() {
        TestingHelper.LIST.add("beforeClass");
    }

    @Test
    public void test1() {
        TestingHelper.LIST.add("test1");
    }

    @Test
    public void test2() {
        TestingHelper.LIST.add("test2");
    }

    @AfterClass
    public static void afterClass() {
        TestingHelper.LIST.add("afterClass");
    }

    public static class TestingHelper extends Exception {
        public static @NotNull List<String> LIST = new LinkedList<>();
    }
}
