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
