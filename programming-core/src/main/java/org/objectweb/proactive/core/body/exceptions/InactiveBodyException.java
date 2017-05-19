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
package org.objectweb.proactive.core.body.exceptions;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;


// end inner class LocalInactiveBody
public class InactiveBodyException extends ProActiveRuntimeException {
    public InactiveBodyException(UniversalBody body) {
        super("Cannot perform this call because body " + body.getID() + " is inactive");
    }

    public InactiveBodyException(UniversalBody body, String nodeURL, UniqueID id, String remoteMethodCallName) {
        // TODO when the class of the remote reified object will be available through UniversalBody, add this info.
        super("Cannot send request \"" + remoteMethodCallName + "\" to Body \"" + id + "\" located at " + nodeURL +
              " because body " + body.getID() + " is inactive");
    }

    public InactiveBodyException(UniversalBody body, String localMethodName) {
        super("Cannot serve method \"" + localMethodName + "\" because body " + body.getID() + " is inactive");
    }

    public InactiveBodyException(String string, Throwable e) {
        super(string, e);
    }
}
