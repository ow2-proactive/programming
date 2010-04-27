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
package org.objectweb.proactive.core.component;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Constant strings used throughout the components implementation.
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface Constants {
    // hierarchical types of component
    public final static String COMPOSITE = "composite";
    public final static String PRIMITIVE = "primitive";

    // controller names
    public final static String ATTRIBUTE_CONTROLLER = "attribute-controller";
    public final static String AUTONOMIC_CONTROLLER = "autonomic-controller";
    public final static String BINDING_CONTROLLER = "binding-controller";
    public final static String COMPONENT = "component";
    public final static String CONTENT_CONTROLLER = "content-controller";
    public final static String CONTROLLER_STATE_DUPLICATION = "controller-state-duplication-controller";
    public final static String GATHERCAST_CONTROLLER = "gathercast-controller";
    public final static String HOST_SETTER_CONTROLLER = "host-setter-controller";
    public final static String LIFECYCLE_CONTROLLER = "lifecycle-controller";
    public final static String MEMBRANE_CONTROLLER = "membrane-controller";
    public final static String MIGRATION_CONTROLLER = "migration-controller";
    public final static String MONITOR_CONTROLLER = "monitor-controller";
    public final static String MULTICAST_CONTROLLER = "multicast-controller";
    public final static String NAME_CONTROLLER = "name-controller";
    public final static String PRIORITY_CONTROLLER = "priority-controller";
    public final static String REQUEST_QUEUE_CONTROLLER = "request-queue-controller";
    public final static String SUPER_CONTROLLER = "super-controller";

    public final static String FACTORY = "factory";
    public final static String TYPE_FACTORY = "type-factory";
    public final static String GENERIC_FACTORY = "generic-factory";
    public final static boolean SYNCHRONOUS = true;
    public final static boolean WITHOUT_CONFIG_FILE = false;
    public final static String CYCLIC_NODE_SUFFIX = "-cyclicInstanceNumber-";
}
