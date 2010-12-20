package org.objectweb.proactive.annotation.multiactivity;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@PublicAPI
public @interface Group {

	public String name();
	
	public boolean selfCompatible();

}
