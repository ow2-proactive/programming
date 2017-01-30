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
package org.objectweb.proactive.extensions.pamr.exceptions;

import java.io.IOException;

import org.objectweb.proactive.extensions.pamr.protocol.AgentID;


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

    private final boolean notifySender;

    private final AgentID recipient;

    private final AgentID faulty;

    public MalformedMessageException() {
        super();
        this.notifySender = false;
        this.recipient = null;
        this.faulty = null;
    }

    public MalformedMessageException(String message) {
        super(message);
        this.notifySender = false;
        this.recipient = null;
        this.faulty = null;
    }

    public MalformedMessageException(Throwable cause) {
        super();
        this.initCause(cause);
        this.notifySender = false;
        this.recipient = null;
        this.faulty = null;
    }

    public MalformedMessageException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
        this.notifySender = false;
        this.recipient = null;
        this.faulty = null;
    }

    public MalformedMessageException(MalformedMessageException original, AgentID recipient, AgentID faulty) {
        super(original.getMessage());
        this.initCause(original);
        this.notifySender = true;
        this.recipient = recipient;
        this.faulty = faulty;
    }

    public MalformedMessageException(MalformedMessageException original, AgentID recipient) {
        super(original.getMessage());
        this.initCause(original);
        this.notifySender = true;
        this.recipient = recipient;
        this.faulty = null;
    }

    /** Notify the message sender of this problem */
    public MalformedMessageException(MalformedMessageException original, boolean notifySender) {
        super(original.getMessage());
        this.initCause(original);
        this.notifySender = notifySender;
        this.recipient = null;
        this.faulty = null;
    }

    public boolean mustNotifySender() {
        return this.notifySender;
    }

    public AgentID getRecipient() {
        return this.recipient;
    }

    public AgentID getFaulty() {
        return this.faulty;
    }

}
