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
package org.objectweb.proactive.core.body;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.BodyNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class store all active bodies known in the current JVM. The class is a singleton
 * in a given JVM. It also associates each active thread with its matching body.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 * @see Body
 * @see UniqueID
 *
 */
public class LocalBodyStore {
    //
    // -- STATIC MEMBERS -----------------------------------------------
    //
    static Logger logger = ProActiveLogger.getLogger(Loggers.BODY);

    private static LocalBodyStore instance = new LocalBodyStore();

    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //

    /**
     * This table maps all known active Bodies in this JVM with their UniqueID
     * From one UniqueID it is possible to get the corresponding body if
     * it belongs to this JVM
     */
    private BodyMap localBodyMap = new BodyMap();

    /**
     * This table maps all known HalfBodies in this JVM with their UniqueID
     * From one UniqueID it is possible to get the corresponding halfbody if
     * it belongs to this JVM
     */
    private BodyMap localHalfBodyMap = new BodyMap();

    private Map<Thread, HalfBody> halfbodiesThreadMap = new ConcurrentHashMap<>();

    /**
     * This table maps all known Forwarder's in this JVM with their UniqueID
     * From one UniqueID it is possible to get the corresponding forwarder if
     * it belongs to this JVM. Used by the HTTP ProActive protocol, so that
     * when a Body migrates, it is still possible to talk to the object using
     * its BodyID, that here now points to the Forwarder instead of the original
     * Body.
     */
    private BodyMap localForwarderMap = new BodyMap();

    /**
     * Static object that manages the registration of listeners and the sending of
     * events
     */

    //    private BodyEventProducerImpl bodyEventProducer = new BodyEventProducerImpl();
    private MetaObjectFactory halfBodyMetaObjectFactory = null;

    /**
     * Executions context associated to the calling thread.
     */
    private ThreadLocal<Stack<Context>> contexts = new ThreadLocal<Stack<Context>>();

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new AbstractBody.
     * Used for serialization.
     */
    private LocalBodyStore() {
    }

    //
    // -- STATIC METHODS -----------------------------------------------
    //
    public static LocalBodyStore getInstance() {
        return instance;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public synchronized MetaObjectFactory getHalfBodyMetaObjectFactory() {
        if (this.halfBodyMetaObjectFactory == null) {
            this.halfBodyMetaObjectFactory = ProActiveMetaObjectFactory.newInstance();
        }
        return this.halfBodyMetaObjectFactory;
    }

    public synchronized void setHalfBodyMetaObjectFactory(MetaObjectFactory factory) {
        this.halfBodyMetaObjectFactory = factory;
    }

    /**
     * Push a new context for the calling thread.
     * @param c the new context
     * @see org.objectweb.proactive.core.body.Context
     */
    public void pushContext(Context c) {
        Stack<Context> s = this.contexts.get();
        if (s == null) {
            // cannot use initValue method
            // see getContext()
            s = new Stack<Context>();
            s.push(c);
            this.contexts.set(s);
        } else {
            s.push(c);
        }
    }

    /**
     * Pop the current context. The current context is popped from the stack
     * associated to the calling thread and returned.
     * @return the current context associated to the calling thread if any, null otherwise.
     * @see org.objectweb.proactive.core.body.Context
     */
    public Context popContext() {
        Stack<Context> stack = this.contexts.get();
        return stack != null ? stack.pop() : null;
    }

    public boolean isInAo() {
        Stack<Context> sc = contexts.get();
        if (sc != null) {
            try {
                UniqueID id = sc.peek().getBody().getID();
                return LocalBodyStore.getInstance().getLocalBody(id) != null;
            } catch (EmptyStackException e) {
                logger.warn("Contexts stack was empty", e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Get the current context. The current context is not removed from the stack
     * associated to the calling thread. If no context is associated with the
     * calling thread, a HalfBody is created for the calling thread, and a new context
     * is pushed for this HalfBody.
     * @return the current context associated to the calling thread.
     * @see org.objectweb.proactive.core.body.Context
     */
    public Context getContext() {
        Stack<Context> s = this.contexts.get();

        // Note that the stack could have been created while being empty for RMI thread that have
        // performed immediate services.
        if ((s == null) || (s.isEmpty())) {
            // If we cannot find a context for the current thread we assume that the current thread
            // is not the one from an active object. Therefore in this case we create an HalfBody
            // that handle the futures, and push a new context for this HalfBody.
            HalfBody body = HalfBody.getHalfBody(this.getHalfBodyMetaObjectFactory());
            s = ((s == null) ? new Stack<Context>() : s);
            Context c = new Context(body, null);
            s.push(c);
            this.contexts.set(s);
            registerHalfBody(body);
            removeUnreferencedHalfBodies();
            removeHalfBodiesOfDeadThreads();

            return c;
        } else {
            return s.peek();
        }
    }

    private void removeHalfBodiesOfDeadThreads() {
        for (Iterator<Map.Entry<Thread, HalfBody>> i = halfbodiesThreadMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry<Thread, HalfBody> entry = i.next();
            if (!entry.getKey().isAlive()) {
                unregisterHalfBodies(Collections.singletonList(entry.getValue()));
                i.remove();
            }
        }
    }

    private void unregisterHalfBodies(List<HalfBody> toRemove) {
        for (HalfBody halfBody : toRemove) {
            LocalBodyStore.getInstance().unregisterHalfBody(halfBody);
            try {
                halfBody.terminate();
            } catch (Exception e) {
                logger.warn("Error when terminating half body " + halfBody.name, e);
            }

        }
    }

    private boolean isReferenced(UniversalBody body) {
        for (UniversalBody referencedBody : halfbodiesThreadMap.values()) {
            if (referencedBody.getID().equals(body.getID())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all halfBodies not referenced in the thread map
     */
    private void removeUnreferencedHalfBodies() {
        List<HalfBody> toRemove = new ArrayList<>();
        for (Iterator<UniversalBody> i = localHalfBodyMap.bodiesIterator(); i.hasNext();) {
            HalfBody halfBody = (HalfBody) i.next();
            if (!isReferenced(halfBody)) {
                toRemove.add(halfBody);
            }
        }

        unregisterHalfBodies(toRemove);
    }

    /**
     * Delete the stack of context associated to the calling thread.
     * @see org.objectweb.proactive.core.body.Context
     */
    public void clearAllContexts() {
        this.contexts.remove();
    }

    /**
     * Returns the body belonging to this JVM whose ID is the one specified.
     * Returns null if a body with such an id is not found in this jvm
     * @param bodyID the ID to look for
     * @return the body with matching id or null
     */
    public Body getLocalBody(UniqueID bodyID) {
        return (Body) this.localBodyMap.getBody(bodyID);
    }

    /**
     * Returns the halfbody belonging to this JVM whose ID is the one specified.
     * Returns null if a halfbody with such an id is not found in this jvm
     * @param bodyID the ID to look for
     * @return the halfbody with matching id or null
     */
    public Body getLocalHalfBody(UniqueID bodyID) {
        return (Body) this.localHalfBodyMap.getBody(bodyID);
    }

    /**
     * Returns the forwarder belonging to this JVM whose ID is the one specified.
     * Returns null if a forwarder with such an id is not found in this jvm
     * @param bodyID the ID to look for
     * @return the halfbody with matching id or null
     */
    public Body getForwarder(UniqueID bodyID) {
        return (Body) this.localForwarderMap.getBody(bodyID);
    }

    /**
     * Returns all local Bodies in a new BodyMap
     * @return all local Bodies in a new BodyMap
     */
    public BodyMap getLocalBodies() {
        return (BodyMap) this.localBodyMap.clone();
    }

    /**
     * Returns the number of bodies registered in this store.
     * @return the number of bodies registered in this store.
     */
    public int getLocalBodiesCount() {
        return this.localBodyMap.size();
    }

    /**
     * Returns all local HalfBodies in a new BodyMap
     * @return all local HalfBodies in a new BodyMap
     */
    public BodyMap getLocalHalfBodies() {
        return (BodyMap) this.localHalfBodyMap.clone();
    }

    /**
     * Returns the number of HalfBodies registered in this store.
     * @return the number of HalfBodies registered in this store.
     */
    public int getLocalHalfBodiesCount() {
        return this.localHalfBodyMap.size();
    }

    //
    // -- FRIENDLY METHODS -----------------------------------------------
    //
    void registerBody(AbstractBody body) {
        if (this.localBodyMap.getBody(body.getID()) != null) {
            logger.debug("Body already registered in the body map");
        }
        localBodyMap.putBody(body.bodyID, body);

        // JMX Notification
        if (!body.isProActiveInternalObject) {
            ProActiveRuntimeWrapperMBean mbean = ProActiveRuntimeImpl.getProActiveRuntime().getMBean();
            if (mbean != null) {
                mbean.sendNotification(NotificationType.bodyCreated,
                                       new BodyNotificationData(body.getID(), body.getNodeURL(), body.getName()));
            }
        }

        // END JMX Notification
    }

    void unregisterBody(AbstractBody body) {
        localBodyMap.removeBody(body.bodyID);

        // JMX Notification
        if (!body.isProActiveInternalObject) {
            ProActiveRuntimeWrapperMBean mbean = ProActiveRuntimeImpl.getProActiveRuntime().getMBean();
            if (mbean != null) {
                mbean.sendNotification(NotificationType.bodyDestroyed,
                                       new BodyNotificationData(body.getID(), body.getNodeURL(), body.getName()));
            }
        }

        // END ProActiveEvent
        if ((this.localBodyMap.size() == 0) && CentralPAPropertyRepository.PA_EXIT_ON_EMPTY.isTrue()) {
            PALifeCycle.exitSuccess();
        }
    }

    void registerHalfBody(HalfBody body) {
        halfbodiesThreadMap.put(Thread.currentThread(), body);
        this.localHalfBodyMap.putBody(body.bodyID, body);
    }

    void unregisterHalfBody(AbstractBody body) {
        this.localHalfBodyMap.removeBody(body.bodyID);
    }

    public void registerForwarder(AbstractBody body) {
        if (this.localForwarderMap.getBody(body.bodyID) != null) {
            logger.debug("Forwarder already registered in the body map");
            this.localForwarderMap.removeBody(body.bodyID);
        }
        this.localForwarderMap.putBody(body.bodyID, body);
    }

    public void unregisterForwarder(AbstractBody body) {
        this.localForwarderMap.removeBody(body.bodyID);
    }
}
