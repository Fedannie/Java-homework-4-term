import annotations.*;
import exceptions.AfterClassFailedException;
import exceptions.BeforeClassFailedException;
import exceptions.UnknownTestException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reports.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**Class that runs tests of given class.*/
public class Runner {
    /**Given class test of which should be run.*/
    private final @NotNull Class<?> testClass;
    /**Methods with @BeforeClass annotation.*/
    private final @NotNull List<Method> beforeClassMethods;
    /**Methods with @Before annotation.*/
    private final @NotNull List<Method> beforeMethods;
    /**Methods with @Test annotation.*/
    private final @NotNull List<Method> testMethods;
    /**Methods with @After annotation.*/
    private final @NotNull List<Method> afterMethods;
    /**Methods with @AfterClass annotation.*/
    private final @NotNull List<Method> afterClassMethods;

    /**
     * Public constructor. Gets all methods from {@code testClass},
     * sort them in Methods lists.
     * @param testClass -- given test class.
     */
    public Runner(final @NotNull Class<?> testClass) {
        this.testClass = testClass;
        beforeClassMethods = new ArrayList<>();
        beforeMethods = new ArrayList<>();
        testMethods = new ArrayList<>();
        afterMethods = new ArrayList<>();
        afterClassMethods = new ArrayList<>();

        for (Method method : testClass.getMethods()){
            if (method.getAnnotation(BeforeClass.class) != null) {
                beforeClassMethods.add(method);
            }

            if (method.getAnnotation(Before.class) != null) {
                beforeMethods.add(method);
            }

            if (method.getAnnotation(Test.class) != null) {
                testMethods.add(method);
            }

            if (method.getAnnotation(After.class) != null) {
                afterMethods.add(method);
            }

            if (method.getAnnotation(AfterClass.class) != null) {
                afterClassMethods.add(method);
            }
        }
    }

    /**
     * Performs testing. Invoke all test methods in annotated order.
     * @return list of reports which contain information about execution.
     * @throws BeforeClassFailedException when there an error appears while running method annotated as @BeforeClass
     * @throws UnknownTestException when there are any problems with the structure of tested class.
     * @throws AfterClassFailedException when there an error appears while running method annotated as @AfterClass
     */
    @NotNull
    public List<Report> test() throws BeforeClassFailedException, UnknownTestException, AfterClassFailedException {
        List<Report> reports = new ArrayList<>();

        try {
            invokeMethods(null, beforeClassMethods);
        } catch (InvocationTargetException e) {
            throw new BeforeClassFailedException();
        } catch (NullPointerException e) {
            throw new UnknownTestException(e.getMessage());
        }

        for (Method method : testMethods) {
            reports.add(runTestMethod(method));
        }

        try {
            invokeMethods(null, afterClassMethods);
        } catch (InvocationTargetException e) {
            throw new AfterClassFailedException();
        }  catch (NullPointerException e) {
            throw new UnknownTestException(e.getMessage());
        }

        return reports;
    }

    /**
     * Invoke given methods.
     * @param instance -- instance of tested class
     * @param methods -- methods that should be invoked
     * @throws InvocationTargetException when there is an error in invoked method.
     * @throws UnknownTestException  when there are any problems with the structure of tested class.
     */
    private void invokeMethods(final @Nullable Object instance, final @NotNull List<Method> methods)
            throws InvocationTargetException, UnknownTestException{
        for (Method method : methods) {
            try {
                method.invoke(instance);
            } catch (Exception e) {
                throw new UnknownTestException(e.getMessage());
            }
        }
    }

    /**
     * Run one test method.
     * @param method -- which method should be invoked
     * @return report with information about execution of the test.
     * @throws UnknownTestException  when there are any problems with the structure of tested class.
     */
    @NotNull
    private Report runTestMethod(final @NotNull Method method) throws UnknownTestException {
        Test testAnnotation = method.getAnnotation(Test.class);

        String ignoreReason = testAnnotation.ignore();
        if (!ignoreReason.equals(Test.noIgnoranceDescription)) {
            return new IgnoredReport(testClass.getName(), method.getName(), ignoreReason);
        }

        Object instance;
        try {
            instance = testClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new UnknownTestException(e.getMessage());
        }

        long estimatedTime = System.currentTimeMillis();
        Throwable exception = null;
        try {
            invokeMethods(instance, beforeMethods);
            method.invoke(instance);
            invokeMethods(instance, afterMethods);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new UnknownTestException(e.getMessage());
        } catch (Exception e) {
            exception = e;
        }
        estimatedTime = System.currentTimeMillis() - estimatedTime;

        if (exception != null && !testAnnotation.expected().isInstance(exception)) {
            return new UnexpectedExceptionReport(testClass.getName(), method.getName(), exception);
        } else if (exception == null && !testAnnotation.expected().equals(Test.None.class)) {
            return new ExpectedExceptionNotFoundReport(testClass.getName(), method.getName(),
                    testAnnotation.expected().getName());
        }

        return new PassedReport(testClass.getName(), method.getName(), estimatedTime);
    }
}
