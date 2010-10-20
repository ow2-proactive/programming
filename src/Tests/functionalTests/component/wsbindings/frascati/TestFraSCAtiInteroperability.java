/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
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
package functionalTests.component.wsbindings.frascati;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.webservices.WSInfo;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;


/*
 * This test is ignored because it implies calls to non ProActive services, ie. external web services provided by
 * FraSCAti (https://wiki.ow2.org/frascati/). As there is no way to guarantee the permanent availability of the
 * FraSCAti server, this test is thus disabled by default.
 *
 * In this test, a client interface of a GCM component is bound to a FraSCAti web service (Available at
 * http://frascati-ws.lille.inria.fr/Services). This web service implements the methods of the
 * functionalTests.component.wsbindings.frascati.Services interface which also corresponds to the client interface
 * signature of the GCM component. All methods are then called in order to ensure the interoperability between
 * ProActive and FraSCAti.
 */
@Ignore
public class TestFraSCAtiInteroperability {
    public static final String FRASCATI_WS_URL = "http://frascati-ws.lille.inria.fr/Services(" +
        WSInfo.CXFAEGISWSCALLER_ID + ")";

    protected Component client;
    protected Services services;

    @Before
    public void setUp() throws Exception {
        CentralPAPropertyRepository.GCM_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");
        Factory factory = FactoryFactory.getFactory();
        client = (Component) factory.newComponent("functionalTests.component.wsbindings.frascati.Client",
                null);
        GCM.getBindingController(client).bindFc(ClientImpl.SERVICES_NAME, FRASCATI_WS_URL);
        GCM.getGCMLifeCycleController(client).startFc();
        services = ((Services) client.getFcInterface("server-services"));
    }

    @Test(timeout = 20000)
    public void testDoNothing() {
        services.doNothing();
    }

    @Test(timeout = 20000)
    public void testIncrementInt() {
        int i = 1;
        int result = services.incrementInt(i);
        assertNotNull(i);
        assertEquals(i + Services.INCREMENT_VALUE, result);
    }

    @Test(timeout = 20000)
    public void testDecrementDouble() {
        double[] arrayDouble = new double[5];
        for (int i = 0; i < arrayDouble.length; i++) {
            arrayDouble[i] = i + (0.1 * i);
        }
        double[] result = services.decrementArrayDouble(arrayDouble);
        assertNotNull(result);
        assertEquals(arrayDouble.length, result.length);
        for (int i = 0; i < arrayDouble.length; i++) {
            assertEquals(arrayDouble[i] - Services.DECREMENT_VALUE, result[i], 0);
        }
    }

    @Test(timeout = 20000)
    public void testModifyString() {
        String string = "ProActive";
        String result = services.modifyString(string);
        assertNotNull(result);
        assertEquals(string + Services.STRING_MODIFIER, result);
    }

    @Test(timeout = 20000)
    public void testSplitString() {
        String string = "Hello FraSCAti !";
        String[] result = services.splitString(string);
        assertNotNull(result);
        assertArrayEquals(string.split(Services.SPLIT_REGEX), result);
    }

    @Test(timeout = 20000)
    public void testModifyObject() {
        AnObject anObject = new AnObject();
        anObject.setId("Id" + ((int) (Math.random() * 100)));
        anObject.setIntField((int) (Math.random() * 100));
        AnObject result = services.modifyObject(anObject);
        anObject.setId(anObject.getId() + Services.STRING_MODIFIER);
        anObject.setIntField(anObject.getIntField() + Services.INCREMENT_VALUE);
        assertNotNull(result);
        assertEquals(anObject, result);
    }

    @Test(timeout = 20000)
    public void testModifyArrayObject() {
        AnObject[] arrayObject = new AnObject[5];
        for (int i = 0; i < arrayObject.length; i++) {
            AnObject anObject = new AnObject();
            anObject.setId("Id" + ((int) (Math.random() * 100)));
            anObject.setIntField((int) (Math.random() * 100));
            arrayObject[i] = anObject;
        }
        AnObject[] result = services.modifyArrayObject(arrayObject);
        for (int i = 0; i < arrayObject.length; i++) {
            arrayObject[i].setId(arrayObject[i].getId() + Services.STRING_MODIFIER);
            arrayObject[i].setIntField(arrayObject[i].getIntField() + Services.INCREMENT_VALUE);
        }
        assertNotNull(result);
        assertArrayEquals(arrayObject, result);
    }
}
