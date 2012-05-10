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
package org.objectweb.proactive.examples.jmx.remote.management.transactions;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.management.NotificationBroadcasterSupport;

import org.objectweb.proactive.examples.jmx.remote.management.command.CommandMBean;
import org.objectweb.proactive.examples.jmx.remote.management.exceptions.TransactionNotActiveException;
import org.objectweb.proactive.examples.jmx.remote.management.status.Status;


public abstract class Transaction extends NotificationBroadcasterSupport {
    public static final long ACTIVE = 0;
    public static final long COMMITED = 1;
    public static final long DEAD = 2;
    protected long idTransaction;
    protected ConcurrentLinkedQueue<CommandMBean> commandQueue;
    protected long state;

    public abstract Status executeCommand(CommandMBean c);

    public abstract long getId();

    public abstract Status commit() throws TransactionNotActiveException;

    public abstract Status rollback() throws TransactionNotActiveException;

    public long getState() {
        return this.state;
    }

    public abstract Vector<CommandMBean> getCommands();

    public abstract void removeInCompensation(Vector<CommandMBean> commands);

    public abstract ArrayList<CommandMBean> getCompensation();

    public abstract void compensate();
}
