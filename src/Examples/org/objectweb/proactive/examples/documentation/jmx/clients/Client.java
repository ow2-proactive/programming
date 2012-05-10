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
package org.objectweb.proactive.examples.documentation.jmx.clients;

import java.io.IOException;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.client.ClientConnector;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.examples.documentation.jmx.listeners.MyListener;


public class Client {

    /**
     * @param args
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws NodeException
     * @throws ActiveObjectCreationException
     * @throws InstanceNotFoundException
     */
    public static void main(String[] args) throws MalformedObjectNameException, NullPointerException,
            IOException, ActiveObjectCreationException, NodeException, InstanceNotFoundException,
            AttributeNotFoundException {
        //@snippet-start JMX_ClientConnector_1
        ClientConnector cc = new ClientConnector("//localhost/", "MyServerName");
        //@snippet-end JMX_ClientConnector_1
        //@snippet-start JMX_ClientConnector_2
        // Connects the client connector
        cc.connect();

        // Retrieves the ProActive connection
        ProActiveConnection pc = cc.getConnection();

        // Creates an ObjectName refering to an MBean
        ObjectName beanName = new ObjectName("SimpleAgent:name=hellothere");
        //@snippet-break JMX_ClientConnector_2

        //@snippet-start JMX_ClientConnector_3
        /* Creates an active listener MyListener */
        MyListener listener = (MyListener) PAActiveObject.newActive(MyListener.class.getName(), null);

        /* Adds the listener to the Mbean server where we are connected to */
        pc.addNotificationListener(beanName, listener, null, null);
        //@snippet-end JMX_ClientConnector_3
        //@snippet-resume JMX_ClientConnector_2

        // Asynchronously invoke the "concat" method of this mbean with two Strings as parameters
        GenericTypeWrapper<?> returnedObject = pc.invokeAsynchronous(beanName, "concat", new Object[] {
                "FirstString ", "| SecondString" }, new String[] { String.class.getName(),
                String.class.getName() });
        //@snippet-end JMX_ClientConnector_2
        System.out.println("Returned = " + returnedObject.getObject());

        Attribute attribute = new Attribute("Message", "This is my new message");
        AttributeList al = new AttributeList();
        al.add(attribute);
        pc.setAttributesAsynchronous(beanName, al);

        //@snippet-start JMX_ClientConnector_4
        JMXNotificationManager.getInstance().subscribe(
                ProActiveRuntimeImpl.getProActiveRuntime().getMBean().getObjectName(), listener);
        //@snippet-end JMX_ClientConnector_4

        ProActiveRuntimeImpl.getProActiveRuntime().getMBean().sendNotification(
                NotificationType.GCMRuntimeRegistered, new String("Ca marche"));

        //@snippet-start JMX_ClientConnector_5
        JMXNotificationManager.getInstance().unsubscribe(
                ProActiveRuntimeImpl.getProActiveRuntime().getMBean().getObjectName(), listener);
        //@snippet-end JMX_ClientConnector_5
        returnedObject = pc.invokeAsynchronous(beanName, "concat", new Object[] { "FirstString ",
                "| SecondString" }, new String[] { String.class.getName(), String.class.getName() });
        System.out.println("Returned = " + returnedObject.getObject());

    }

}
