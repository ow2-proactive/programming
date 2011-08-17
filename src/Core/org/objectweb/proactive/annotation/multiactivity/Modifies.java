package org.objectweb.proactive.annotation.multiactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;

/**
 * This annotation is a contract. The listed variables will be modified by the
 * annotated method.
 * <br>
 * The variables not listed in this annotation are either not accessed at all, or
 * just read, if they appear in the Reads annotation.
 * @author Zsolt Istvan
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PublicAPI
@Deprecated
public @interface Modifies {
	public String[] value();
}
