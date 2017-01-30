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
package org.objectweb.proactive;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStrategy;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;


/**
 * <P>
 * An object implementing this interface is an implementation of the non functional part of an
 * ActiveObject. This representation is local to the ActiveObject. By contrast there is a remote
 * representation of Body that can be accessed by distant object.
 * </P>
 * <P>
 * <code>UniversalBody</code> defines the remote accessible part of the body while
 * <code>LocalBody</code> defines the local accessible part of the body.
 * </P>
 * <P>
 * The body of an ActiveObject provides needed services such as a the ability to sent and receive
 * request and reply.
 * </P>
 * <P>
 * The interface also defines how the activity methods of an active object sees its Body.
 * </P>
 * <P>
 * A body has 2 associated states :
 * <ul>
 * <li>alive : the body is alive as long as it is processing request and reply</li>
 * <li>active : the body is active as long as it has an associated thread running to serve the
 * requests by calling methods on the active object.</li>
 * </ul>
 * </P>
 * <P>
 * Note that a thread can be alive but not active, such as a forwarder that just forward request to
 * another peer.
 * </P>
 * 
 * @author The ProActive Team
 * @version 1.0, 2001/10/23
 * @since ProActive 0.9
 */
@PublicAPI
//@snippet-start body
public interface Body extends LocalBodyStrategy, UniversalBody {

    /**
     * Returns whether the body is alive or not. The body is alive as long as it is processing
     * request and reply
     * 
     * @return whether the body is alive or not.
     */
    public boolean isAlive();

    /**
     * Returns whether the body is active or not. The body is active as long as it has an associated
     * thread running to serve the requests by calling methods on the active object.
     * 
     * @return whether the body is active or not.
     */
    public boolean isActive();

    /**
     * blocks all incoming communications. After this call, the body cannot receive any request or
     * reply.
     */
    public void blockCommunication();

    /**
     * Signals the body to accept all incoming communications. This call undo a previous call to
     * blockCommunication.
     */
    public void acceptCommunication();

    /**
     * Allows the calling thread to enter in the ThreadStore of this body.
     */
    public void enterInThreadStore();

    /**
     * Allows the calling thread to exit from the ThreadStore of this body.
     */
    public void exitFromThreadStore();

    /**
     * Tries to find a local version of the body of id uniqueID. If a local version is found it is
     * returned. If not, tries to find the body of id uniqueID in the known body of this body. If a
     * body is found it is returned, else null is returned.
     * 
     * @param uniqueID
     *            the id of the body to lookup
     * @return the last known version of the body of id uniqueID or null if not known
     */
    public UniversalBody checkNewLocation(UniqueID uniqueID);

    /**
     * Returns the MBean associated to this active object.
     * 
     * @return the MBean associated to this active object.
     */
    public BodyWrapperMBean getMBean();

    // /**
    // * set the policy server of the active object
    // * @param server the policy server
    // */
    // public void setPolicyServer(PolicyServer server);

    /**
     * Set the nodeURL of this body
     * 
     * @param newNodeURL
     *            the new URL of the node
     */
    public void updateNodeURL(String newNodeURL);

    /**
     * For setting an immediate service for this body. An immediate service is a method that will be
     * executed by the calling thread, or by a dedicated per-caller thread if uniqueThread is true.
     * 
     * @param methodName
     *            the name of the method
     * @param uniqueThread true if this immediate service should be always executed by the same thread for 
     * 			  a given caller, false if any thread can be used.
     */
    public void setImmediateService(String methodName, boolean uniqueThread);

    /**
     * For setting an immediate service for this body. An immediate service is a method that will be
     * executed by the calling thread.
     *
     * @param methodName the name of the method
     *
     * @deprecated Replaced by {@link #setImmediateService(String, boolean)}
     */
    @Deprecated
    public void setImmediateService(String methodName);

    /**
     * Removes an immediate service for this body An immediate service is a method that will be
     * executed by the calling thread.
     * 
     * @param methodName
     *            the name of the method
     */
    public void removeImmediateService(String methodName);

    /**
     * Adds an immediate service for this body An immediate service is a method that will be
     * executed by the calling thread, or by a dedicated per-caller thread if uniqueThread is true.
     * 
     * @param methodName
     *            the name of the method
     * @param parametersTypes
     *            the types of the parameters of the method
     * @param uniqueThread true if this immediate service should be always executed by the same thread for 
     * 			  a given caller, false if any thread can be used.
     *            
     */
    public void setImmediateService(String methodName, Class<?>[] parametersTypes, boolean uniqueThread);

    /**
     * Removes an immediate service for this body An immediate service is a method that will be
     * executed by the calling thread.
     * 
     * @param methodName
     *            the name of the method
     * @param parametersTypes
     *            the types of the parameters of the method
     */
    public void removeImmediateService(String methodName, Class<?>[] parametersTypes);

    /**
     * Terminate the body. After this call the body is no more alive and no more active. The body is
     * unusable after this call. If some automatic continuations are registered in the futurepool of
     * this body, the ACThread will be killed when the last registered AC is sent.
     */
    public void terminate();

    /**
     * @param completeACs
     *            if true, this call has the same behavior than terminate(). Otherwise, the ACThread
     *            is killed even if some ACs remain in the futurepool.
     * @see #terminate(). If completeACs is true, this call has the same behavior as terminate().
     *      Otherwise, the ACThread is killed even if some ACs remain in the futurepool.
     */
    public void terminate(boolean completeACs);

    /**
     * Checks if a method methodName is declared by the reified object AND the method has the same
     * parameters as parametersTypes Note that the called method should be <i>public</i>, since
     * only the public methods can be called on an active object. Note also that a call to
     * checkMethod(methodName, null) is different to a call to checkMethod(methodName, new Class[0])
     * The former means that no checking is done on the parameters, whereas the latter means that we
     * look for a method with no parameters.
     * 
     * @param methodName
     *            the name of the method
     * @param parametersTypes
     *            an array of parameter types
     * @return true if the method exists, false otherwise
     */
    public boolean checkMethod(String methodName, Class<?>[] parametersTypes);

    /**
     * Checks if a method methodName is declared by the reified object Note that the called method
     * should be <i>public</i>, since only the public methods can be called on an active object.
     * Note that this call is strictly equivalent to checkMethod(methodName, null);
     * 
     * @param methodName
     *            the name of the method
     * @return true if the method exists, false otherwise
     */
    public boolean checkMethod(String methodName);

    public void registerIncomingFutures();
}
//@snippet-end body
