package org.objectweb.proactive.annotation.multiactivity;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.objectweb.proactive.annotation.PublicAPI;

/**
 * This annotation represents the definition of a method group.
 * The compatibility rules that apply on groups can be defined 
 * using the DefineRules construct.
 * @author Zsolt Istvan
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@PublicAPI
public @interface Group {
	
	/**
	 * A representative name of the group. This has to be unique for the class.
	 * @return
	 */
	public String name();
	
	/**
	 * Flag that shows if the methods contained in this group can run in parallel or not.
	 * @return
	 */
	public boolean selfCompatible();
	
	public String parameter() default "";
	
	public String comparator() default "";

}
