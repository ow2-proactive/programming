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
package functionalTests.component;

import java.io.PrintStream;
import java.io.Serializable;


/**
 * @author The ProActive Team
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 52;

    /**
     *
     */
    String message;
    boolean valid = true;

    public Message() {
    }

    public Message(String string) {
        message = string;
    }

    public Message append(String string) {
        message = message + string;
        return this;
    }

    public Message append(Message message) {
        if (isValid()) {
            this.message = message + message.toString();
        }
        return this;
    }

    @Override
    public String toString() {
        return message;
    }

    public void setInvalid() {
        message = null;
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public void printToStream(PrintStream out) {
        out.println(message);
    }

    public String getMessage() {
        return message;
    }
}
