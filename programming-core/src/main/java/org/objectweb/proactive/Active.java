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
package org.objectweb.proactive;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <P>
 * Active is the root of the all interfaces related to the activity of an
 * active object. It is a convenience for having a common interface that
 * can be used to type objects implementing one or more of the others.
 * </P><P>
 * So far we considered three steps in the lifecycle of an active object.
 * </P>
 * <ul>
 * <li> the initialization of the activity (done only once)</li>
 * <li> the activity itself</li>
 * <li> the end of the activity (unique event)</li>
 * </ul>
 * <P>
 * In case of a migration, an active object stops and restarts its activity
 * automatically without invoking the init or ending phases. Only the
 * activity itself is restarted.
 * </P>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/06
 * @since   ProActive 0.9.3
 */
@PublicAPI
public interface Active {
}
