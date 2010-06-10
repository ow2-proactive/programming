/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.jmx.remote.management.command.osgi;

import org.objectweb.proactive.examples.jmx.remote.management.mbean.BundleInfo;
import org.objectweb.proactive.examples.jmx.remote.management.osgi.OSGiStore;
import org.objectweb.proactive.examples.jmx.remote.management.status.Status;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.Transaction;


public class UninstallCommand extends OSGiCommand implements UninstallCommandMBean {
    /**
     * 
     */
    private static final long serialVersionUID = 430L;
    private long idBundle;

    public UninstallCommand(Transaction t, long id) {
        super(t, OSGiCommand.UNINSTALL + id, OSGiCommand.UNINSTALL);
        this.idBundle = id;
    }

    public Status undo_() {
        String location = OSGiStore.getInstance().getBundleInfo(this.idBundle);
        InstallCommand c = new InstallCommand(this.transaction, location, OSGiCommand.INSTALL);
        return c.do_();
    }

    public boolean check() {
        BundleInfo[] bundles = OSGiStore.getInstance().getBundles();
        for (int i = 0; i < bundles.length; i++) {
            if (bundles[i].getId() == this.idBundle) {
                this.done = false;
                return this.done;
            }
        }
        this.done = true;
        return true;
    }
}
