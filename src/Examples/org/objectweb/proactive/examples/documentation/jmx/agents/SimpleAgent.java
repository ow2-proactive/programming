package org.objectweb.proactive.examples.documentation.jmx.agents;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.objectweb.proactive.core.jmx.server.ServerConnector;
import org.objectweb.proactive.examples.documentation.jmx.mbeans.Hello;


public class SimpleAgent {
    private MBeanServer mbs = null;

    public SimpleAgent() {

        // Get the platform MBeanServer
        mbs = ManagementFactory.getPlatformMBeanServer();

        // Unique identification of MBeans
        Hello helloBean = new Hello();

        ObjectName helloName = null;
        try {
            // Uniquely identify the MBeans and register them with the platform MBeanServer
            helloName = new ObjectName("SimpleAgent:name=hellothere");
            mbs.registerMBean(helloBean, helloName);

            //@snippet-start JMX_ServerConnector
            ServerConnector serverConnector = new ServerConnector("MyServerName");
            serverConnector.start();
            //@snippet-end JMX_ServerConnector

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Utility method: so that the application continues to run
    private static void waitForEnterPressed() {
        try {
            System.out.println("Press  to continue...");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) {
        SimpleAgent agent = new SimpleAgent();
        System.out.println("SimpleAgent is running...");
        SimpleAgent.waitForEnterPressed();
    }
}
