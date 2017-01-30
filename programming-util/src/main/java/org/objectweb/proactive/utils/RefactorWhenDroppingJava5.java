/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * A marker to identify non optimal code intended to support Java 5.
 * 
 * This marker helps to identify following code:
 * <ul>
 *  <li>Code that could be prettier if Java 6 API was used instead of Java 5 </li>
 *  <li>Code that use a degraded mode for Java 5</li>
 *  <li>Code that compile with Java 5 but does not work at runtime</li> 
 * </ul>
 * 
 * @author ProActive team
 * @since  ProActive 5.1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.METHOD,
          ElementType.PARAMETER, ElementType.TYPE })
public @interface RefactorWhenDroppingJava5 {

}
