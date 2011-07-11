/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.examples.jmx.remote.management.command.osgi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Date;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.objectweb.proactive.examples.jmx.remote.management.command.CommandMBean;
import org.objectweb.proactive.examples.jmx.remote.management.exceptions.JMXException;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.IJmx;
import org.objectweb.proactive.examples.jmx.remote.management.osgi.OSGiStore;
import org.objectweb.proactive.examples.jmx.remote.management.status.Status;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.Transaction;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;


public abstract class OSGiCommand extends NotificationBroadcasterSupport implements IJmx, CommandMBean,
        Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    public static final String INSTALL = "install ";
    public static final String UNINSTALL = "uninstall ";
    public static final String START = "start ";
    public static final String STOP = "stop ";
    public static final String UPDATE = "update ";
    protected String operation;
    protected Date date;
    protected boolean done = false;
    protected transient BundleContext bundleContext;
    protected Transaction transaction;
    protected String type;

    /**
     *
     * @param bundleContext TODO
     * @param bundle
     */
    public OSGiCommand(Transaction transaction, String operation, String type) {
        this.date = new Date(System.currentTimeMillis());
        this.operation = operation;
        this.transaction = transaction;
        this.type = type;
    }

    /**
     *
     * @return
     */
    public Date getDate() {
        return this.date;
    }

    /**
     *
     * @throws BundleException
     */
    public Status undo() {
        return new Status(Status.ERR, OSGiStore.getInstance().getUrl(), this.operation, "unavailable");
    }

    public boolean hasBeenDone() {
        return this.done;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[" + OSGiStore.getInstance().getUrl() + "]" + this.operation;
    }

    public Status do_() {
        String outString = "";
        String errString = "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        PrintStream psErr = new PrintStream(baosErr);
        try {
            OSGiStore.getInstance().getShell().executeCommand(this.operation, ps, psErr);

            outString = baos.toString();
            errString = baosErr.toString();

            if (!errString.equals("")) {
                return new Status(Status.ERR, this.operation, errString, OSGiStore.getInstance().getUrl());
            } else {

                Status s = new Status(Status.OK, this.operation, outString, OSGiStore.getInstance().getUrl());
                return s;
            }
        } catch (Exception e) {
            //                e.printStackTrace();
            return new Status(Status.ERR, this.operation, e.getMessage(), OSGiStore.getInstance().getUrl());
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (baosErr != null) {
                try {
                    baosErr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getOperation() {
        return this.operation;
    }

    public boolean equals(CommandMBean c) {
        return (c.getOperation().equals(this.operation));
    }

    /**
     * IJmx implementation
     * @throws JMXException
     */
    public void register() throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException, JMXException {
        ObjectName on;
        try {
            String s = "Transactions:id=" + this.transaction.getId() + ",date=" + this.date.getTime() +
                ",command=" + this.type +
                this.operation.substring(this.operation.lastIndexOf(File.separatorChar) + 1);
            on = new ObjectName(s);
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, on);
        } catch (MalformedObjectNameException e) {
            throw new JMXException(e);
        } catch (NullPointerException e) {
            throw new JMXException(e);
        }
    }

    public void unregister() throws InstanceNotFoundException, MBeanRegistrationException, JMXException {
        ObjectName on;

        try {
            on = new ObjectName("Transactions:id" + this.transaction.getId() + ",command=" + this.operation);
        } catch (MalformedObjectNameException e) {
            throw new JMXException(e);
        } catch (NullPointerException e) {
            throw new JMXException(e);
        }
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(on);
    }
}
