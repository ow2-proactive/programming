/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Annotation for specifying the reduction mechanism of a given method.
 * <br>
 * Examples:
 * <br>
 * Reduce the result using the SELECT_UNIQUE_VALUE mode:
 * <pre>
 * &#064;Reduce(reductionMode = ReduceMode.Select_Unique_Value)
 * public IntWrapper doIt();
 *</pre>
 *
 * @author The ProActive Team
 */
@PublicAPI
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Reduce {

    //	String closureCode();

    /**
     * Return the reduction mode used.
     *
     * @return the reduction mode used.
     */
    ReduceMode reductionMode();

    Class<?> customReductionMode() default ReduceMode.class;

}
