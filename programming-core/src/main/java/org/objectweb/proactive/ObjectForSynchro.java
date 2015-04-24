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
package org.objectweb.proactive;

/**
 * <P>
 * An object instance of this class is to be returned when a method of an active
 * object wants to let the caller wait synchronously the end of the execution of the
 * method. No real data is expected as a result, but the caller can used the object
 * returned to wait the actual execution (service) of the request.
 * </p><p>
 * In order to wait the method <code>waitFor</code> of <code>ProActive</code> can be used
 * </p>
 * <pre>
 * Object sync = A.m();     // m returns a future of an ObjectForSynchro
 * ProActive.waitFor(sync); // perform a wait until the ObjectForSynchro if returned
 * </pre>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class ObjectForSynchro extends Object implements java.io.Serializable {

    private static final long serialVersionUID = 62L;

    /**
     * No arg constructor for Serializable
     */
    public ObjectForSynchro() {
    }
}
