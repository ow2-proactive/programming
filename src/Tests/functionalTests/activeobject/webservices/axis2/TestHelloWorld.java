/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package functionalTests.activeobject.webservices.axis2;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.client.AbstractClientFactory;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.client.ClientFactory;
import org.objectweb.proactive.extensions.webservices.exceptions.UnknownFrameWorkException;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;

import functionalTests.FunctionalTest;
import functionalTests.activeobject.webservices.common.Couple;
import functionalTests.activeobject.webservices.common.HelloWorld;


public class TestHelloWorld extends FunctionalTest {

    private String url;
    private WebServices ws;

    @org.junit.Before
    public void deployHelloWorld() {
        try {
            this.url = AbstractWebServicesFactory.getLocalUrl();
            HelloWorld hw = (HelloWorld) PAActiveObject.newActive(
                    "functionalTests.activeobject.webservices.common.HelloWorld", new Object[] {});

            WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory("axis2");
            ws = wsf.getWebServices(url);

            ws.exposeAsWebService(hw, "HelloWorld");

            Method m1 = hw.getClass().getSuperclass().getMethod("sayText");
            Method m2 = hw.getClass().getSuperclass().getMethod("putTextToSay",
                    new Class<?>[] { String.class });
            Method[] methods = new Method[] { m1, m2 };

            ws.exposeAsWebService(hw, "HelloWorldMethods", methods);

            String[] methodNames = new String[] { "putTextToSay", "sayText" };

            ws.exposeAsWebService(hw, "HelloWorldMethodNames", methodNames);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @org.junit.Test
    public void testHelloWorld() {

        ClientFactory cf = null;
        try {
            cf = AbstractClientFactory.getClientFactory("axis2");
        } catch (UnknownFrameWorkException e1) {
            e1.printStackTrace();
            assertTrue(false);
        }

        try {

            Client client = cf.getClient(this.url, "HelloWorld", HelloWorld.class);

            client.oneWayCall("putHelloWorld", null);

            Object[] response = client.call("contains", new Object[] { "Hello world!" }, boolean.class);

            assertTrue((Boolean) response[0]);

            client.oneWayCall("putTextToSay", new Object[] { "Good bye world!" });

            response = client.call("sayText", null, String.class);

            String text = (String) response[0];
            assertTrue(text.equals("Hello world!"));

            response = client.call("sayText", null, String.class);

            text = (String) response[0];
            assertTrue(text.equals("Good bye world!"));

            response = client.call("sayText", null, String.class);

            text = (String) response[0];
            assertTrue(text.equals("The list is empty"));

            response = client.call("sayHello", null, String.class);

            text = (String) response[0];
            assertTrue(text.equals("Hello!"));

            Couple cpl1 = new Couple();
            cpl1.setMyInt(1);
            cpl1.setStr1("First");
            Couple cpl2 = new Couple();
            cpl2.setMyInt(2);
            cpl2.setStr1("Second");
            Couple[] couples = new Couple[] { cpl1, cpl2 };

            client.oneWayCall("setCouples", new Object[] { couples });

            response = client.call("getCouples", null, Array.newInstance(Couple.class, 2).getClass());

            Couple[] table = (Couple[]) response[0];
            Couple test1 = table[0];
            Couple test2 = table[1];

            assertTrue(test1.getMyInt() == 1);
            assertTrue(test1.getStr1().equals("First"));
            assertTrue(test2.getMyInt() == 2);
            assertTrue(test2.getStr1().equals("Second"));

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

        try {

            Client client = cf.getClient(this.url, "HelloWorldMethods", HelloWorld.class);

            client.oneWayCall("putTextToSay", new Object[] { "Hi ProActive Team!" });

            Object[] response = client.call("sayText", null, String.class);

            String text = (String) response[0];
            assertTrue(text.equals("Hi ProActive Team!"));

            response = client.call("sayText", null, String.class);

            text = (String) response[0];
            assertTrue(text.equals("The list is empty"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

        try {
            Client client = cf.getClient(this.url, "HelloWorldMethods", HelloWorld.class);

            // This call should raise an exception
            // since the method putHelloWorld has not been
            // exposed
            client.oneWayCall("putHelloWorld", null);
            assertTrue(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            Client client = cf.getClient(this.url, "HelloWorldMethodNames", HelloWorld.class);

            client.oneWayCall("putTextToSay", new Object[] { "Hi ProActive Team!" });

            Object[] response = client.call("sayText", null, String.class);

            String text = (String) response[0];
            assertTrue(text.equals("Hi ProActive Team!"));

            response = client.call("sayText", null, String.class);

            text = (String) response[0];
            assertTrue(text.equals("The list is empty"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

        try {
            Client client = cf.getClient(this.url, "HelloWorldMethodNames", HelloWorld.class);

            // This call should raise an exception
            // since the putHelloWorld method has
            // not been exposed
            client.oneWayCall("putHelloWorld", null);
            assertTrue(false);
        } catch (WebServicesException e) {
            e.printStackTrace();
        }
    }

    @org.junit.After
    public void undeployHelloWorld() {
        try {
            ws.unExposeAsWebService("HelloWorld");
            ws.unExposeAsWebService("HelloWorldMethods");
            ws.unExposeAsWebService("HelloWorldMethodNames");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
