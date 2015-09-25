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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedReplyException;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedRequestException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.tags.LocalMemoryTag;
import org.objectweb.proactive.core.body.tags.MessageTagsFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.group.spmd.ProActiveSPMDGroupManager;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.util.HeartbeatResponse;
import org.objectweb.proactive.core.util.ThreadStore;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class gives a common implementation of the Body interface. It provides all the non specific
 * behavior allowing sub-class to write the detail implementation.
 * </p>
 * <p>
 * Each body is identify by an unique identifier.
 * </p>
 * <p>
 * All active bodies that get created in one JVM register themselves into a table that allows to
 * tack them done. The registering and deregistering is done by the AbstractBody and the table is
 * managed here as well using some static methods.
 * </p>
 * <p>
 * In order to let somebody customize the body of an active object without subclassing it,
 * AbstractBody delegates lot of tasks to satellite objects that implements a given interface.
 * Abstract protected methods instantiate those objects allowing subclasses to create them as they
 * want (using customizable factories or instance).
 * </p>
 * 
 * @author The ProActive Team
 * @version 1.0, 2001/10/23
 * @since ProActive 0.9
 * @see Body
 * @see UniqueID
 * 
 */
public abstract class AbstractBody extends AbstractUniversalBody implements Body, Serializable {
    //
    // -- STATIC MEMBERS -----------------------------------------------
    //
    private static Logger logger = ProActiveLogger.getLogger(Loggers.BODY);

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    protected ThreadStore threadStore;

    // the current implementation of the local view of this body
    protected LocalBodyStrategy localBodyStrategy;

    // SPMD GROUP
    protected ProActiveSPMDGroupManager spmdManager;

    // JMX
    /** The MBean representing this body */
    protected BodyWrapperMBean mbean;
    protected boolean isProActiveInternalObject = false;

    // MESSAGE-TAGS Factory
    protected MessageTagsFactory messageTagsFactory;
    protected Map<String, LocalMemoryTag> localMemoryTags;

    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //

    /** whether the body has an activity done with a active thread */
    private transient volatile boolean isActive;

    /**
     * whether the body has been killed. A killed body has no more activity although stopping the
     * activity thread is not immediate
     */
    private transient volatile boolean isDead;

    /**
     * Activity thread of this body
     */
    private transient volatile Thread activityThread;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new AbstractBody. Used for serialization.
     */
    public AbstractBody() {
    }

    /**
     * Creates a new AbstractBody for an active object attached to a given node.
     * 
     * @param reifiedObject
     *            the active object that body is for
     * @param nodeURL
     *            the URL of the node that body is attached to
     * @param factory
     *            the factory able to construct new factories for each type of meta objects needed
     *            by this body
     */
    public AbstractBody(Object reifiedObject, String nodeURL, MetaObjectFactory factory)
            throws ActiveObjectCreationException {
        super(reifiedObject, nodeURL);

        this.threadStore = factory.newThreadStoreFactory().newThreadStore();

        // GROUP
        this.spmdManager = factory.newProActiveSPMDGroupManagerFactory().newProActiveSPMDGroupManager();

        // MESSAGE TAGS
        this.messageTagsFactory = factory.newRequestTagsFactory();
        this.localMemoryTags = new ConcurrentHashMap<String, LocalMemoryTag>();
    }

    public BodyWrapperMBean getMBean() {
        return this.mbean;
    }

    public String getReifiedClassName() {
        return this.localBodyStrategy.getReifiedObject().getClass().getName();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        if (this.localBodyStrategy != null) {
            return "Body for " + this.getName() + " node=" + this.nodeURL + " id=" + this.bodyID;
        }

        return "Method call called during Body construction -- the body is not yet initialized ";
    }

    /**
     * Returns a short string representation of this object.
     *
     * @return a string representation of this object
     */
    public String shortString() {
        return "" + this.nodeURL + "/" + this.bodyID;
    }

    //
    // -- implements UniversalBody -----------------------------------------------
    //
    public void receiveRequest(Request request) throws java.io.IOException {
        // System.out.println("" + this + " --> receiveRequest m="+request.getMethodName());        
        try {
            this.enterInThreadStore();
            if (this.isDead) {
                throw new BodyTerminatedRequestException(shortString(),
                    request != null ? request.getMethodName() : null);
            }
            this.registerIncomingFutures();
            this.internalReceiveRequest(request);
        } finally {
            this.exitFromThreadStore();
        }
    }

    public void receiveReply(Reply reply) throws java.io.IOException {
        // System.out.println(" --> receiveReply m="+reply.getMethodName());
        try {
            enterInThreadStore();
            if (this.isDead && (this.getFuturePool() == null)) {
                throw new BodyTerminatedReplyException(reifiedObjectClassName,
                    reply != null ? reply.getMethodName() : null);
            }
            this.registerIncomingFutures();
            internalReceiveReply(reply);
        } finally {
            exitFromThreadStore();
        }
    }

    /**
     * This method effectively register futures (ie in the futurePool) that arrive in this active
     * object (by parameter or by result). Incoming futures have been registered in the static table
     * FuturePool.incomingFutures during their deserialization. This effective registration must be
     * perform AFTER entering in the ThreadStore.
     */
    public void registerIncomingFutures() {
        // get list of futures that should be deserialized and registered "behind the ThreadStore"
        java.util.ArrayList<Future> incomingFutures = FuturePool.getIncomingFutures();

        if (incomingFutures != null) {
            // if futurePool is not null, we are in an Active Body
            if (getFuturePool() != null) {
                // some futures have to be registered in the local futurePool
                java.util.Iterator<Future> it = incomingFutures.iterator();
                while (it.hasNext()) {
                    Future current = it.next();
                    getFuturePool().receiveFuture(current);
                }
                FuturePool.removeIncomingFutures();
            } else {
                // we are in a migration forwarder,just remove registered futures
                FuturePool.removeIncomingFutures();
            }
        }
    }

    public void enableAC() {
        this.localBodyStrategy.getFuturePool().enableAC();
    }

    public void disableAC() {
        this.localBodyStrategy.getFuturePool().disableAC();
    }

    public ProActiveSPMDGroupManager getProActiveSPMDGroupManager() {
        return this.spmdManager;
    }

    //
    // -- implements Body -----------------------------------------------
    //
    public void terminate() {
        this.terminate(true);
    }

    public void terminate(boolean completeACs) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling terminate for active object " + this.reifiedObjectClassName);
        }
        if (this.isDead && (this.getFuturePool() == null)) {
            return;
        }

        this.isDead = true;
        // the ACthread is not killed if completeACs is true AND there is
        // some ACs remaining...
        activityStopped(completeACs && this.getFuturePool().remainingAC());
        this.remoteBody = null;
        // unblock is thread was block
        acceptCommunication();

        // JMX unregistration
        if (mbean != null) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = mbean.getObjectName();
            if (mbs.isRegistered(objectName)) {
                try {
                    mbs.unregisterMBean(objectName);
                } catch (InstanceNotFoundException e) {
                    logger.error("The MBean with the objectName " + objectName + " was not found", e);
                } catch (MBeanRegistrationException e) {
                    logger.error("The MBean with the objectName " + objectName +
                        " can't be unregistered from the MBean server", e);
                }
            }
            this.mbean = null;
        }

        // END JMX unregistration

        try {
            super.roe.unexportAll();
        } catch (ProActiveException e) {
            logger.error("Failed to unexport " + this.getID(), e);
        }

        try {
            super.roe.unregisterAll();
        } catch (ProActiveException e) {
            logger.error("Failed to unregister " + this.getID(), e);
        }
    }

    public void blockCommunication() {
        this.threadStore.close();
    }

    public void acceptCommunication() {
        this.threadStore.open();
    }

    public void enterInThreadStore() {
        this.threadStore.enter();
    }

    public void exitFromThreadStore() {
        this.threadStore.exit();
    }

    public boolean isAlive() {
        return !this.isDead;
    }

    public boolean isActive() {
        return this.isActive;
    }

    /**
     * interrupts the current request serving, this will not terminate the body
     */
    public void interruptService() {
        if (activityThread == null) {
            throw new IllegalStateException("Body is inactive");
        }
        activityThread.interrupt();
    }

    public UniversalBody checkNewLocation(UniqueID bodyID) {
        // we look in the location table of the current JVM
        Body body = LocalBodyStore.getInstance().getLocalBody(bodyID);
        if (body != null) {
            // we update our table to say that this body is local
            this.location.updateBody(bodyID, body);
            return body;
        } else {
            // it was not found in this vm let's try the location table
            return this.location.getBody(bodyID);
        }
    }

    // public void setPolicyServer(PolicyServer server) {
    // if (server != null) {
    // if ((this.securityManager != null) &&
    // (this.securityManager.getPolicyServer() == null)) {
    // this.securityManager = new ProActiveSecurityManager(EntityType.UNKNOWN, server);
    // this.isSecurityOn = true;
    // logger.debug("Security is on " + this.isSecurityOn);
    // // this.securityManager.setBody(this);
    // }
    // }
    // }

    //
    // -- implements LocalBody -----------------------------------------------
    //
    public FuturePool getFuturePool() {
        return this.localBodyStrategy.getFuturePool();
    }

    public BlockingRequestQueue getRequestQueue() {
        return this.localBodyStrategy.getRequestQueue();
    }

    public Object getReifiedObject() {
        return this.localBodyStrategy.getReifiedObject();
    }

    /**
     * Serves the request. The request should be removed from the request queue before serving,
     * which is correctly done by all methods of the Service class. However, this condition is not
     * ensured for custom calls on serve.
     */
    public void serve(Request request) {
        // Serve        
        this.localBodyStrategy.serve(request);
    }

    /**
     * Serves the request with the given exception as result instead of the normal execution.
     * The request should be removed from the request queue before serving,
     * which is correctly done by all methods of the Service class. However, this condition is
     * not ensured for custom calls on serve.
     */
    public void serveWithException(Request request, Throwable exception) {
        // Serve
        this.localBodyStrategy.serveWithException(request, exception);
    }

    public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody)
            throws IOException {
        // Tag the outgoing request with the barrier tags
        if (!this.spmdManager.isTagsListEmpty()) {
            methodCall.setBarrierTags(this.spmdManager.getBarrierTags());
        }
        this.localBodyStrategy.sendRequest(methodCall, future, destinationBody);
    }

    public Object receiveHeartbeat() {
        return this.isAlive() ? HeartbeatResponse.OK : HeartbeatResponse.IS_DEAD;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Receives a request for later processing. The call to this method is non blocking unless the
     * body cannot temporary receive the request.
     * 
     * @param request
     *            the request to process
     * @exception java.io.IOException
     *                if the request cannot be accepted
     */
    protected abstract void internalReceiveRequest(Request request) throws java.io.IOException;

    /**
     * Receives a reply in response to a former request.
     * 
     * @param reply
     *            the reply received
     * @exception java.io.IOException
     *                if the reply cannot be accepted
     */
    protected abstract void internalReceiveReply(Reply reply) throws java.io.IOException;

    protected void setLocalBodyImpl(LocalBodyStrategy localBody) {
        this.localBodyStrategy = localBody;
    }

    /**
     * Signals that the activity of this body, managed by the active thread has just stopped.
     * 
     * @param completeACs
     *            if true, and if there are remaining AC in the futurepool, the AC thread is not
     *            killed now; it will be killed after the sending of the last remaining AC.
     */
    protected void activityStopped(boolean completeACs) {
        if (!this.isActive) {
            return;
        }
        this.isActive = false;

        // tries to interrupt the current request, or unblock any wait
        interruptService();

        this.activityThread = null;

        // We are no longer an active body
        LocalBodyStore.getInstance().unregisterBody(this);

        // Thus, contexts are no more needed
        LocalBodyStore.getInstance().clearAllContexts();

        // JMX unregistration
        if (this.mbean != null) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = this.mbean.getObjectName();
            if (mbs.isRegistered(objectName)) {
                try {
                    mbs.unregisterMBean(objectName);
                } catch (InstanceNotFoundException e) {
                    logger.error("The MBean with the objectName " + objectName + " was not found", e);
                } catch (MBeanRegistrationException e) {
                    logger.error("The MBean with the objectName " + objectName +
                        " can't be unregistered from the MBean server", e);
                }
            }
            this.mbean = null;
        }

        // END JMX unregistration
    }

    /**
     * Signals that the activity of this body, managed by the active thread has just started.
     */
    protected void activityStarted() {
        if (this.isActive) {
            return;
        }
        isActive = true;
        activityThread = Thread.currentThread();
        // Set the initial context : we associated this body to the thread running it
        LocalBodyStore.getInstance().pushContext(new Context(this, null));

        // we register in this JVM
        LocalBodyStore.getInstance().registerBody(this);
    }

    /**
     * Set the SPMD group for the active object
     * 
     * @param o -
     *            the new SPMD group
     */
    public void setSPMDGroup(Object o) {
        this.spmdManager.setSPMDGroup(o);
    }

    /**
     * Returns the SPMD group of the active object
     * 
     * @return the SPMD group of the active object
     */
    public Object getSPMDGroup() {
        return this.spmdManager.getSPMDGroup();
    }

    /**
     * Returns the size of of the SPMD group
     * 
     * @return the size of of the SPMD group
     */
    public int getSPMDGroupSize() {
        return PAGroup.size(this.getSPMDGroup());
    }

    // MESSAGE TAGS MEMORY
    /**
     * Create a local memory for the specified tag for a lease period inferior to 
     * the max lease period defined in properties.
     * @param id    - Tag Identifier
     * @param lease - Lease period of the memroy
     * @return The LocalMemoryTag 
     */
    public LocalMemoryTag createLocalMemoryTag(String id, int lease) {
        int maxLease = CentralPAPropertyRepository.PA_MAX_MEMORY_TAG_LEASE.getValue();
        lease = (lease > maxLease) ? maxLease : lease;
        this.localMemoryTags.put(id, new LocalMemoryTag(id, lease));
        return this.localMemoryTags.get(id);
    }

    /**
     * Return the local memory of the specified Tag
     * @param id - Tag identifer
     * @return the LocalMemoryTag of the specified Tag
     */
    public LocalMemoryTag getLocalMemoryTag(String id) {
        return this.localMemoryTags.get(id);
    }

    /**
     * Clear the local memory of the specified Tag
     * @param id - Tag identifier
     */
    public void clearLocalMemoryTag(String id) {
        this.localMemoryTags.remove(id);
    }

    /**
     * To get the localMemoryTag Map of the body
     * @return Map<String, {@link LocalMemoryTag}> the LocalMemoryTags
     */
    public Map<String, LocalMemoryTag> getLocalMemoryTags() {
        return this.localMemoryTags;
    }

    /**
     * Returns true if an immediate service request is being served now.
     */
    public abstract boolean isInImmediateService() throws IOException;

    //
    // -- SERIALIZATION METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();

        mbean = null;
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
