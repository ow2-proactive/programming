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
package org.objectweb.proactive.core.body.exceptions;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;


// end inner class LocalInactiveBody
public class InactiveBodyException extends ProActiveRuntimeException {

    private static final long serialVersionUID = 60L;
    public InactiveBodyException(UniversalBody body) {
        super("Cannot perform this call because body " + body.getID() + "is inactive");
    }

    public InactiveBodyException(UniversalBody body, String nodeURL, UniqueID id, String remoteMethodCallName) {
        // TODO when the class of the remote reified object will be available through UniversalBody, add this info.
        super("Cannot send request \"" + remoteMethodCallName + "\" to Body \"" + id + "\" located at " +
            nodeURL + " because body " + body.getID() + " is inactive");
    }

    public InactiveBodyException(UniversalBody body, String localMethodName) {
        super("Cannot serve method \"" + localMethodName + "\" because body " + body.getID() + " is inactive");
    }

    public InactiveBodyException(String string, Throwable e) {
        super(string, e);
    }
}
