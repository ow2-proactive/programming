package org.objectweb.proactive.annotation.multiactivity;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.objectweb.proactive.annotation.PublicAPI;

/**
 * This annotation represents a method group.
 * The compatibility rules that apply on groups can be defined 
 * using the {@link DefineRules} annotation.
 * @author Zsolt Istvan
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@PublicAPI
public @interface Group {
	
	/**
	 * A representative name of the group. This has to be unique for a class and its predecessors.
	 * @return
	 */
	public String name();
	
	/**
	 * Flag that shows if the methods contained in this group can run in parallel or not.
	 * @return
	 */
	public boolean selfCompatible();
	
	/**
	 * Class name of the common argument of all methods belonging to this group.
	 * @return
	 */
	public String parameter() default "";
	
	/**
	 * Conditioning function of the self-compatibility.
	 * @return
	 */
	public String condition() default "";

}
