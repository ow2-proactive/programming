/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.body.ft.servers.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.httpserver.ClassServerServlet;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 * @since 2.2
 */
public abstract class CheckpointServerImpl implements CheckpointServer {
    // logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE);

    // used memory
    private final static Runtime runtime = Runtime.getRuntime();

    // global server
    protected FTServer server;

    // ClassServerServlet assiociated codebase
    protected String codebase;

    // The stable storage (idCheckpointer --> [list of] [checkpoints])
    protected Hashtable<UniqueID, List<Checkpoint>> checkpointStorage;

    /**
     *
     */
    public CheckpointServerImpl(FTServer server) {
        this.server = server;

        this.checkpointStorage = new Hashtable<UniqueID, List<Checkpoint>>();

        if (PAProperties.PA_CLASSLOADING_USEHTTP.isTrue()) {
            ClassServerServlet.get();
            this.codebase = ClassServerServlet.get().getCodeBase();
        } else {
            // URL must be prefixed by pa tu use our custom protocol handlers
            // URL must be terminated by a / according to the RMI specification
            this.codebase = "pa" + ProActiveRuntimeImpl.getProActiveRuntime().getURL() + "/";
        }

        try {
            NodeFactory.getDefaultNode();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getServerCodebase()
     */
    public String getServerCodebase() throws RemoteException {
        return this.codebase;
    }

    // UTIL METHODS
    protected long getSize(Serializable c) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // serialize the body
            oos.writeObject(c);
            // store the serialized form
            return baos.toByteArray().length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /*
     * Return the memory actually used For debugging stuff.
     */
    protected long getUsedMem() {
        return (CheckpointServerImpl.runtime.totalMemory() - CheckpointServerImpl.runtime.freeMemory()) / 1024;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#initialize()
     */
    public void initialize() throws RemoteException {
        this.checkpointStorage = new Hashtable<UniqueID, List<Checkpoint>>();
    }
}
