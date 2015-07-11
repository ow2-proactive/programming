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
package org.objectweb.proactive.core.group;

import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;


/**
 * This class provides multithreading for the creation of active objects.
 *
 * @author The ProActive Team
 */
public class ProcessForGroupCreation extends AbstractProcessForGroup implements Runnable {
    private ProxyForGroup proxyGroup;
    private String className;
    private Class<?>[] genericParameters;
    private Object[] param;
    private Node node;
    private CountDownLatch doneSignal;

    public ProcessForGroupCreation(ProxyForGroup proxyGroup, String className, Class<?>[] genericParameters,
            Object[] param, Node node, int groupIndex, CountDownLatch doneSignal) {
        this.proxyGroup = proxyGroup;
        this.className = className;
        this.genericParameters = genericParameters;
        this.param = param;
        this.node = node;
        this.groupIndex = groupIndex;
        this.doneSignal = doneSignal;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        try {
            this.proxyGroup.set(this.groupIndex,
                    PAActiveObject.newActive(className, genericParameters, param, node));
        } catch (Exception e) {
            e.printStackTrace();
            // FIXME throw exception (using Callable task)
        }
        doneSignal.countDown();
    }

    @Override
    public int getMemberListSize() {
        return 1;
    }
}
