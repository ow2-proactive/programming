/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.extra.messagerouting.exceptions;

import java.io.IOException;


/**
 * This exception should be thrown each time a message
 * which does not meet the Message Routing Protocol format is
 * encountered
 *
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class MalformedMessageException extends IOException {

    public MalformedMessageException() {
        super();
    }

    public MalformedMessageException(String message) {
        super(message);
    }

    public MalformedMessageException(Throwable cause) {
        super();
        this.initCause(cause);
    }

    public MalformedMessageException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }

}
