package org.objectweb.proactive.annotation.multiactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.multiactivity.compatibility.AnnotationProcessor;

/**
 * This annotation can be used to express parallel compatibility between groups.
 * It is to be used inside a {@link DefineRules} annotation, and defines a set of method groups (identified by name) 
 * which can run in parallel. Optionally, a conditioning function can be defined (see {@link AnnotationProcessor} for details on this function).
 * @author Zsolt Istvan
 */ 
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PublicAPI
public @interface Compatible {
	
	/**
	 * List of group names that can run in parallel.
	 * @return
	 */
	public String[] value();
	
	/**
	 * Conditioning function of the compatibility rule.
	 * @return
	 */
	public String condition() default "";

}
