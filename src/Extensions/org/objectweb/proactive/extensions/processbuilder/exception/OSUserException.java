/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
