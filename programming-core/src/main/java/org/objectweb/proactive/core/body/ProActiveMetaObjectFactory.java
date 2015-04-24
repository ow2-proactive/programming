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
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.reply.ReplyReceiverFactory;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.request.RequestQueueFactory;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiverFactory;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.body.tags.MessageTagsFactory;
import org.objectweb.proactive.core.group.spmd.ProActiveSPMDGroupManager;
import org.objectweb.proactive.core.group.spmd.ProActiveSPMDGroupManagerFactory;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.util.ThreadStore;
import org.objectweb.proactive.core.util.ThreadStoreFactory;
import org.objectweb.proactive.core.util.converter.MakeDeepCopy;


// TODO JAVADOC SHOULD BE REWRITTEN
/**
 * <p>
 * This class provides singleton instances of all default factories
 * creating MetaObjects used in the Body.
 * </p>
 *
 * <b>Since version 1.8, it is also possible to parameterized the factories on a per-object basis. </b>
 * In that case,  public ProActiveMetaObjectFactory(Hashtable parameters) is the constructor to use.
 * <p>
 * One can inherit from this class in order to provide custom implementation
 * of one or several factories. This class provide a default implementation that
 * makes the factories a singleton. One instance of each mata object factory is
 * created when this object is built and the same instance is returned each time
 * somebody ask for an instance.
 * </p>
 * <p>
 * In order to change one meta object factory following that singleton pattern,
 * only the protected method <code>newXXXSingleton</code> has to be overwritten.
 * The method <code>newXXXSingleton</code> is guarantee to be called only once at
 * construction time of this object.
 * </p>
 * <p>
 * In order to change one meta object factory that does not follow the singleton
 * pattern, the public method <code>newXXX</code> has to be overwritten in order
 * to return a new instance of the factory each time. The default implementation
 * of each <code>newXXX</code> method if to return the singleton instance of the
 * factory created from <code>newXXXSingleton</code> method call.
 * </p>
 * <p>
 * Each sub class of this class should be implemented as a singleton and provide
 * a static method <code>newInstance</code> for this purpose.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
@PublicAPI
//@snippet-start proactivemetaobjectfactory
public class ProActiveMetaObjectFactory implements MetaObjectFactory, java.io.Serializable, Cloneable {

    private static final long serialVersionUID = 62L;

    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    // private static final MetaObjectFactory instance = new ProActiveMetaObjectFactory();
    private static MetaObjectFactory instance = new ProActiveMetaObjectFactory();
    public Map<String, Object> parameters = new HashMap<String, Object>();

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    protected RequestFactory requestFactoryInstance;
    protected ReplyReceiverFactory replyReceiverFactoryInstance;
    protected RequestReceiverFactory requestReceiverFactoryInstance;
    protected RequestQueueFactory requestQueueFactoryInstance;

    //    protected RemoteBodyFactory remoteBodyFactoryInstance;
    protected ThreadStoreFactory threadStoreFactoryInstance;
    protected ProActiveSPMDGroupManagerFactory proActiveSPMDGroupManagerFactoryInstance;
    protected MessageTagsFactory requestTagsFactoryInstance;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    protected ProActiveMetaObjectFactory() {
        this.requestFactoryInstance = newRequestFactorySingleton();
        this.replyReceiverFactoryInstance = newReplyReceiverFactorySingleton();
        this.requestReceiverFactoryInstance = newRequestReceiverFactorySingleton();
        this.requestQueueFactoryInstance = newRequestQueueFactorySingleton();
        this.threadStoreFactoryInstance = newThreadStoreFactorySingleton();
        this.proActiveSPMDGroupManagerFactoryInstance = newProActiveSPMDGroupManagerFactorySingleton();
        this.requestTagsFactoryInstance = newRequestTagsFactorySingleton();
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    public static MetaObjectFactory newInstance() {
        return instance;
    }

    public static void setNewInstance(MetaObjectFactory mo) {
        instance = mo;
    }

    /**
     * getter for the parameters of the factory (per-active-object config)
     * @return the parameters of the factory
     */
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    //
    // -- implements MetaObjectFactory -----------------------------------------------
    //
    public RequestFactory newRequestFactory() {
        return this.requestFactoryInstance;
    }

    public ReplyReceiverFactory newReplyReceiverFactory() {
        return this.replyReceiverFactoryInstance;
    }

    public RequestReceiverFactory newRequestReceiverFactory() {
        return this.requestReceiverFactoryInstance;
    }

    public RequestQueueFactory newRequestQueueFactory() {
        return this.requestQueueFactoryInstance;
    }

    //    public RemoteBodyFactory newRemoteBodyFactory() {
    //        return this.remoteBodyFactoryInstance;
    //    }
    public ThreadStoreFactory newThreadStoreFactory() {
        return this.threadStoreFactoryInstance;
    }

    public ProActiveSPMDGroupManagerFactory newProActiveSPMDGroupManagerFactory() {
        return this.proActiveSPMDGroupManagerFactoryInstance;
    }

    public MessageTagsFactory newRequestTagsFactory() {
        return this.requestTagsFactoryInstance;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected RequestFactory newRequestFactorySingleton() {
        return new RequestFactoryImpl();
    }

    protected ReplyReceiverFactory newReplyReceiverFactorySingleton() {
        return new ReplyReceiverFactoryImpl();
    }

    protected RequestReceiverFactory newRequestReceiverFactorySingleton() {
        return new RequestReceiverFactoryImpl();
    }

    protected RequestQueueFactory newRequestQueueFactorySingleton() {
        return new RequestQueueFactoryImpl();
    }

    //    protected RemoteBodyFactory newRemoteBodyFactorySingleton() {
    //        return new RemoteBodyFactoryImpl();
    //    }
    protected ThreadStoreFactory newThreadStoreFactorySingleton() {
        return new ThreadStoreFactoryImpl();
    }

    protected ProActiveSPMDGroupManagerFactory newProActiveSPMDGroupManagerFactorySingleton() {
        return new ProActiveSPMDGroupManagerFactoryImpl();
    }

    protected MessageTagsFactory newRequestTagsFactorySingleton() {
        return new MessageTagsFactoryImpl();
    }

    //  //
    //  // -- INNER CLASSES -----------------------------------------------
    //  //
    protected static class RequestFactoryImpl implements RequestFactory, java.io.Serializable {

    private static final long serialVersionUID = 62L;
        public Request newRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay,
                long sequenceID, MessageTags tags) {
            //########### exemple de code pour les nouvelles factories
            //			if(System.getProperty("migration.stategy").equals("locationserver")){
            //				  return new RequestWithLocationServer(methodCall, sourceBody,
            //                isOneWay, sequenceID, LocationServerFactory.getLocationServer());
            //			}else{
            return new org.objectweb.proactive.core.body.request.RequestImpl(methodCall, sourceBody,
                isOneWay, sequenceID, tags);
            //}
        }
    }

    // end inner class RequestFactoryImpl
    protected static class ReplyReceiverFactoryImpl implements ReplyReceiverFactory, java.io.Serializable {

    private static final long serialVersionUID = 62L;
        public ReplyReceiver newReplyReceiver() {
            return new org.objectweb.proactive.core.body.reply.ReplyReceiverImpl();
        }
    }

    // end inner class ReplyReceiverFactoryImpl
    protected class RequestReceiverFactoryImpl implements RequestReceiverFactory, java.io.Serializable {

    private static final long serialVersionUID = 62L;
        public RequestReceiver newRequestReceiver() {
            return new org.objectweb.proactive.core.body.request.RequestReceiverImpl();
        }
    }

    // end inner class RequestReceiverFactoryImpl
    protected class RequestQueueFactoryImpl implements RequestQueueFactory, java.io.Serializable {

    private static final long serialVersionUID = 62L;
        public BlockingRequestQueue newRequestQueue(UniqueID ownerID) {
            return new org.objectweb.proactive.core.body.request.BlockingRequestQueueImpl(ownerID);
        }
    }

    // end inner class RemoteBodyFactoryImpl
    protected static class ThreadStoreFactoryImpl implements ThreadStoreFactory, java.io.Serializable {

    private static final long serialVersionUID = 62L;
        public ThreadStore newThreadStore() {
            return new org.objectweb.proactive.core.util.ThreadStoreImpl();
        }
    }

    // end inner class ThreadStoreFactoryImpl
    protected static class ProActiveSPMDGroupManagerFactoryImpl implements ProActiveSPMDGroupManagerFactory,
            java.io.Serializable {

    private static final long serialVersionUID = 62L;
        public ProActiveSPMDGroupManager newProActiveSPMDGroupManager() {
            return new ProActiveSPMDGroupManager();
        }
    }

    // REQUEST-TAGS
    protected static class MessageTagsFactoryImpl implements MessageTagsFactory, Serializable {

    private static final long serialVersionUID = 62L;

        /**
         * @see MessageTagsFactory#newMessageTags()
         */
        public MessageTags newMessageTags() {
            return new MessageTags();
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {

        try {
            return MakeDeepCopy.WithObjectStream.makeDeepCopy(this);
        } catch (IOException e) {
            //TODO replace by CloneNotSupportedException(Throwable e) java 1.6
            throw (CloneNotSupportedException) new CloneNotSupportedException(e.getMessage()).initCause(e);
        } catch (ClassNotFoundException e) {
            throw (CloneNotSupportedException) new CloneNotSupportedException(e.getMessage()).initCause(e);
        }
    }
}
//@snippet-end proactivemetaobjectfactory
