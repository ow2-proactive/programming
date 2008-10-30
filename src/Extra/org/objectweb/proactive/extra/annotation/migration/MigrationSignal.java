/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extra.annotation.migration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation can be used on a method that is a migration signal for an Active Object.
 * It is known that, in order to migrate, an Active Object must export a public method in which
 * it must call the static method {@link org.objectweb.proactive.api.PAMobileAgent.migrateTo}.
 * This is because the method call <code>PAMobileAgent.migrateTo</code> must be executed within the thread
 * attached to the body of the Active Object that has to migrate.
 * When a method is annotated with <code>@MigrationSignal</code> , the behaviour  is the following:
 * <ul>
 * 	<li> If the enclosing class definition is not annotated with the
 * 		{@link org.objectweb.proactive.extra.annotation.activeobject.ActiveObject}
 * 		annotation, then we conclude that the user doesn't want to use the object
 * 		as an active object, so migration has no meaning on this object. We signal this
 * 		to the user.
 * 	</li>
 * 	<li> We check whether the method is public. It makes no sense for the method not to be public,
 * as it is supposed to be called when we want to make a migration request on the active object.</li>
 * 	<li>
 * 		If the object is annotated with <code>ActiveObject</code>, then	we check if the annotated method
 * 			contains a call to the moveTo method:
 * 		<ul>
 * 			<li> If the method code contains a call to moveTo, we check that this call
 * 		is the last statement in the method body. If not, an error is signaled to the user</li>
 * 			<li> If the method code does not contain a call to moveTo, the we inform the user that
 * 		she should add the call at the end of the method body.
 * 			</li>
 * 		</ul>
 * 	</li>
 * </ul>
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 3.90
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MigrationSignal {

}
