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
package org.objectweb.proactive.extensions.processbuilder.exception;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * If this exception is throwed by the OSProcessBuilder, 
 * than it was from one of the following reasons:
 * <ul>
 *  <li>User name is incorrect</li>
 *  <li>Password is incorrect</li>
 *  <li>The OSProcessBuilder's internal launching mechanism fails
 *  under the specific user ID - access rights to scripts folder</li>
 * </ul>
 * @author The ProActive Team
 * @since ProActive 5.0.0
 */
@PublicAPI
public class OSUserException extends Exception implements Serializable {

    public OSUserException(String message) {
        super(message);
    }
}
