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
package org.objectweb.proactive.core.component.control;

import java.net.URL;

import org.etsi.uri.gcm.api.control.MigrationException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.identity.PAComponentImpl;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


/**
 * Implementation of the {@link PAMigrationController migration controller}.
 * 
 * @author The ProActive Team
 */
public class PAMigrationControllerImpl extends AbstractPAController implements PAMigrationController {
    /**
     * Creates a {@link PAMigrationControllerImpl}.
     * 
     * @param owner Component owning the controller.
     */
    public PAMigrationControllerImpl(Component owner) {
        super(owner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setControllerItfType() {
        try {
            setItfType(PAGCMTypeFactoryImpl.instance().createFcItfType(Constants.MIGRATION_CONTROLLER,
                    PAMigrationController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller type for controller " +
                this.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrateGCMComponentTo(Object node) throws MigrationException {
        // need to migrate gathercast futures handlers active objects first
        try {
            ((PAComponentImpl) owner).migrateControllersDependentActiveObjectsTo((Node) node);
            PAMobileAgent.migrateTo((Node) node);
        } catch (org.objectweb.proactive.core.body.migration.MigrationException e) {
            throw new MigrationException("Cannot migrate component to the given node", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrateGCMComponentTo(URL nodeUrl) throws MigrationException {
        try {
            migrateGCMComponentTo(NodeFactory.getNode(nodeUrl.toString()));
        } catch (NodeException e) {
            throw new MigrationException("Cannot find node with URL " + nodeUrl);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrateGCMComponentTo(String nodeUrl) throws MigrationException {
        try {
            migrateGCMComponentTo(NodeFactory.getNode(nodeUrl));
        } catch (NodeException e) {
            throw new MigrationException("Cannot find node with URL " + nodeUrl);
        }
    }
}
