package org.objectweb.proactive.annotation.multiactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;

/**
 * This annotation is to be used to define the list of compatibility rules ({@link Compatible}) that 
 * apply to the {@link Group}s defined inside the {@link DefineGroups} annotation.
 * @author Zsolt Istvan
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@PublicAPI
public @interface DefineRules {

	public Compatible[] value();

}
