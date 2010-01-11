/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.ext.locationserver;

import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.migration.MigrationManagerFactory;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * <p>
 * This class overrides the default Factory to provide Request and MigrationManager
 * with location server.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
public class LocationServerMetaObjectFactory extends ProActiveMetaObjectFactory {
    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    private static MetaObjectFactory instance = null;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Constructor for LocationServerMetaObjectFactory.
     */
    protected LocationServerMetaObjectFactory() {
        super();
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    public static synchronized MetaObjectFactory newInstance() {
        if (instance == null) {
            instance = new LocationServerMetaObjectFactory();
        }
        return instance;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected RequestFactory newRequestFactorySingleton() {
        return new RequestWithLocationServerFactory();
    }

    @Override
    protected MigrationManagerFactory newMigrationManagerFactorySingleton() {
        return new MigrationManagerFactoryImpl();
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    protected class RequestWithLocationServerFactory implements RequestFactory, java.io.Serializable {
        transient private LocationServer server = LocationServerFactory.getLocationServer();

        public Request newRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay,
                long sequenceID, MessageTags tags) {
            return new RequestWithLocationServer(methodCall, sourceBody, isOneWay, sequenceID, server, tags);
        }
    }

    protected static class MigrationManagerFactoryImpl implements MigrationManagerFactory,
            java.io.Serializable {
        public MigrationManager newMigrationManager() {
            return new MigrationManagerWithLocationServer(LocationServerFactory.getLocationServer());
        }
    }
}
