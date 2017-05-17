package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation class. This annotation should be used before
 * {@code public void} methods. Annotated methods would start
 * after all {@code @Before} methods and before {@code @After} methods.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Test {
    /**String which denotes that test won't be ignored.*/
    String noIgnoranceDescription = "";

    /**
     * Denotes expected exception which test method should throw.
     * Default value -- class that provides lack of expected exception.
     */
    Class expected() default None.class;

    /**Reason which describes why test method should be ignored.*/
    String ignore() default noIgnoranceDescription;

    /**Denotes no expected exception.*/
    class None {}
}
