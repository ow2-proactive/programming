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
package org.objectweb.proactive.extensions.mixedlocation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationManagerImpl;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.reply.ReplyReceiverForwarder;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiverForwarder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;


public class MigrationManagerWithMixedLocation extends MigrationManagerImpl implements java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.MIGRATION);
    protected UniversalBodyWrapper wrapper;

    public MigrationManagerWithMixedLocation() {
        logger.info("<init> LocationServer is " + locationServer);
    }

    public MigrationManagerWithMixedLocation(LocationServer locationServer) {
        this.migrationCounter = 0;
        if (logger.isDebugEnabled()) {
            logger.debug("LocationServer is " + locationServer);
        }
        this.locationServer = locationServer;
    }

    protected synchronized void createWrapper(UniversalBody remoteBody) {
        if (this.wrapper == null) {
            this.wrapper = new UniversalBodyWrapper(remoteBody, 6000);
        }
    }

    @Override
    public RequestReceiver createRequestReceiver(UniversalBody remoteBody,
            RequestReceiver currentRequestReceiver) {
        this.createWrapper(remoteBody);
        return new RequestReceiverForwarder(wrapper);
    }

    @Override
    public ReplyReceiver createReplyReceiver(UniversalBody remoteBody, ReplyReceiver currentReplyReceiver) {
        this.createWrapper(wrapper);
        return new ReplyReceiverForwarder(wrapper);
    }

    public void updateLocation(Body body) {
        if (locationServer == null) {
            this.locationServer = LocationServerFactory.getLocationServer();
        }
        if (locationServer != null) {
            locationServer.updateLocation(body.getID(), body.getRemoteAdapter());
        }
    }

    @Override
    public void startingAfterMigration(Body body) {
        super.startingAfterMigration(body);
        //we update our location
        this.migrationCounter++;
        if (logger.isDebugEnabled()) {
            logger.debug("XXX counter == " + this.migrationCounter);
        }

        //          if (this.migrationCounter > 3) {
        updateLocation(body);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug("MigrationManagerWithMixedLocation readObject XXXXXXX");
        }
        in.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("MigrationManagerWithMixedLocation writeObject YYYYYY");
        }
        this.locationServer = null;
        out.defaultWriteObject();
    }
}
