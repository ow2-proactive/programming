package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Annotation for specifying the reduction mechanism of a given method.
 * <br>
 * Examples:
 * <br>
 * Reduce the result using the SELECT_UNIQUE_VALUE mode:
 * <pre>
 * &#064;Reduce(reductionMode = ReduceMode.Select_Unique_Value)
 * public IntWrapper doIt();
 *</pre>
 *
 * @author The ProActive Team
 */
@PublicAPI
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Reduce {

    //	String closureCode();

    /**
     * Return the reduction mode used.
     *
     * @return the reduction mode used.
     */
    ReduceMode reductionMode();

    Class<?> customReductionMode() default ReduceMode.class;

}
