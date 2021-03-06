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
            this.proxyGroup.set(this.groupIndex, PAActiveObject.newActive(className, genericParameters, param, node));
        } catch (Exception e) {
            logger.error("", e);
            // FIXME throw exception (using Callable task)
        }
        doneSignal.countDown();
    }

    @Override
    public int getMemberListSize() {
        return 1;
    }
}
