package org.objectweb.proactive.annotation.multiactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;

/**
 * This annotation shows to which group defined at class level (in a DefineGroup construct)
 * a method belongs to.
 * @author Izso
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PublicAPI
public @interface MemberOf {
	
	/**
	 * The name of the group
	 * @return
	 */
	public String value();

}
