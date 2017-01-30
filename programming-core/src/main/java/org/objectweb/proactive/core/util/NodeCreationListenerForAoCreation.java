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
package org.objectweb.proactive.core.util;

import java.util.Vector;
import java.util.concurrent.ExecutorService;

import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;


/**
 * Creates an Active Object by the multi-tread pool when a node is created.
 *
 * @author The ProActive Team
 *
 * Created on Nov 8, 2005
 */
public class NodeCreationListenerForAoCreation implements NodeCreationEventListener {
    private Vector result;

    private String className;

    private Class<?>[] genericParameters;

    private Object[] constructorParameters;

    private ExecutorService threadpool;

    public NodeCreationListenerForAoCreation(Vector result, String className, Class<?>[] genericParameters,
            Object[] constructorParameters, ExecutorService threadpool) {
        this.result = result;
        this.className = className;
        this.genericParameters = genericParameters;
        this.constructorParameters = constructorParameters;
        this.threadpool = threadpool;
    }

    @SuppressWarnings("unchecked")
    public void nodeCreated(NodeCreationEvent event) {
        threadpool.execute(new ProcessForAoCreation(this.result,
                                                    this.className,
                                                    this.genericParameters,
                                                    this.constructorParameters,
                                                    event.getNode()));
    }
}
