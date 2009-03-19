/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>
 * This annotation is put on a class declaration, in order to mark that
 * it will be used to instantiate an active object, and therefore
 * should respect the active object guidelines.
 * </p>
 * <p>Usage:</p>
 * 	<ul>
 * 		<li> When you create a class that you know you will use to instantiate an active object, annotate it using @ActiveObject</li>
 * 		<li> ProActive will check whether your object respect the ProActive coding guidelines - these can be found in the ProActive manual.
 * 			You can take a look especially
 * <a href="http://proactive.inria.fr/release-doc/html/ActiveObjectCreation.html#FutureObjectCreation_commonerror">here</a>.
 * 			The rules are enforced at compile time, in the following ways:
 * 			<ul>
 * 				<li>If you compile from the command line, you should first run <a href ="http://java.sun.com/j2se/1.5.0/docs/guide/apt/index.html">apt</a> before compiling.
 * By default, apt is included in the JDK distributions after 1.5, and is in the same directory as the javac.
 * You can invoke apt just as you invoke javac, using the same javac settings.
 * 				</li>
 * 				<li> If you compile from Eclipse IDE, you can configure it to show you the errors while developping.
 * This can be done by modifying the project settings, in the Java Compiler -> Annotation Processing panel. Please refer to the
 * <a href="TODO">section</a> in the ProActive manual in order to see how this is done.
 * 				</li>
 * 			</ul>
 * 		</li>
 * 	</ul>
 *
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 3.90
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.LOCAL_VARIABLE })
public @interface ActiveObject {

    String virtualNode() default "";

    String logger() default "";
}
