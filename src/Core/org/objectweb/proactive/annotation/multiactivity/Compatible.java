package org.objectweb.proactive.annotation.multiactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;

/**
 * This annotation can be used to express parallel compatibility.
 * It can be used on the level of 
 * <ul>
 * 		<li>a method -- and it will then represent the compatibility of that method
 * with a list of other methods. <br>ATTENTION: The relationship has to be defined bidirectional,
 * so all referenced methods have to declare the first one as compatible also.</li>
 * 		<li>the class, inside a group definition block (DefineGroup) -- when it will 
 * list the groups whose methods can run in parallel.</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PublicAPI
public @interface Compatible {
	
	/**
	 * List of method names or group names, depending on the annotation location.
	 * @return
	 */
	public String[] value();
	
	public String comparator() default "";

}
