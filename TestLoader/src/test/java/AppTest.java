import TestClasses.BeforeAfterClassTest;
import TestClasses.BeforeAfterTest;
import org.junit.Test;
import com.google.common.collect.ImmutableList;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AppTest {
    @Test
    public void beforeAfterTest() throws Exception {
        final Runner testRunner = new Runner(BeforeAfterTest.class);
        testRunner.test();
        assertThat(BeforeAfterTest.TestingHelper.LIST, is(ImmutableList.of("before", "test", "after")));
    }

    @Test
    public void beforeAfterClassTest() throws Exception {
        final Runner testRunner = new Runner(BeforeAfterClassTest.class);
        testRunner.test();
        assertThat(BeforeAfterClassTest.TestingHelper.LIST,
                is(ImmutableList.of("beforeClass", "test1", "test2", "afterClass")));
    }
}
