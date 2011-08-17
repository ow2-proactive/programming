package org.objectweb.proactive.annotation.multiactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;

/**
 * This annotation is a contract. The method that is annotated will 
 * not modify the value of the variables listed. 
 * <br>
 * The ones not listed will be either not accessed at all, or will be 
 * modified if they appear in the Modifies annotation.
 * 
 * @author Zsolt Istvan
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PublicAPI
@Deprecated
public @interface Reads {
	public static final String ALL = "*";
	public String[] value();
}
