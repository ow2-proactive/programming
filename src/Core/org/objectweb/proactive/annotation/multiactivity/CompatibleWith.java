package org.objectweb.proactive.annotation.multiactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;

/* @Inherited? 
 * it is not inherited because in case a subclass overrides 
 * a method, the annotation also has to be rewritten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PublicAPI
/**
 * This annotation is used to express the fact that a method can run in parallel 
 * with a set of other methods of the object.
 * <br/>ATTENTION: This relationship is not bidirectional. For full compatibility 
 * the other method has also to declare this one as compatible.
 */
public @interface CompatibleWith {
	/**
	 * Marks the fact that this method is compatible with all other methods
	 */
	public static final String ALL = "*";
	
	public String[] value();

}
