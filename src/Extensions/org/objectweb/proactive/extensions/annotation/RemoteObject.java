package org.objectweb.proactive.extensions.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>
 * This annotation is put on a class declaration, in order to mark that
 * it will be used to instantiate an remote object.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.LOCAL_VARIABLE })
public @interface RemoteObject {
}
