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
package functionalTests.component.collectiveitf.multicast;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class TesterImpl implements Tester, BindingController {
    MulticastTestItf clientItf;
    MulticastTestItf multicastClientItf = null;

    public void testConnectedServerMulticastItf() {
        List<WrappedInteger> listParameter = new ArrayList<WrappedInteger>();
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            listParameter.add(i, new WrappedInteger(i));
        }
        List<WrappedInteger> result;
        result = clientItf.testBroadcast_Param(listParameter);
        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            Assert.assertTrue(result.contains(new WrappedInteger(i)));
        }

        result = clientItf.testBroadcast_Method(listParameter);
        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            Assert.assertTrue(result.contains(new WrappedInteger(i)));
        }

        result = clientItf.testOneToOne_Param(listParameter);
        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            Assert.assertTrue(result.get(i).equals(new WrappedInteger(i)));
        }

        result = clientItf.testOneToOne_Method(listParameter);
        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            Assert.assertTrue(result.get(i).equals(new WrappedInteger(i)));
        }

        List<WrappedInteger> listForRoundRobin = new ArrayList<WrappedInteger>();
        List<WrappedInteger> expectedResults = new ArrayList<WrappedInteger>();
        for (int i = 0; i < (Test.NB_CONNECTED_ITFS + 1); i++) {
            listForRoundRobin.add(i, new WrappedInteger(i));
            expectedResults.add(i, new WrappedInteger(i % Test.NB_CONNECTED_ITFS));
        }

        result = clientItf.testRoundRobin_Param(listForRoundRobin);
        Assert.assertTrue(result.size() == (Test.NB_CONNECTED_ITFS + 1));
        for (int i = 0; i < result.size(); i++) {
            Assert.assertEquals(result.get(i).getIntValue(), expectedResults.get(i).getIntValue());
        }

        result = clientItf.testRoundRobin_Method(listForRoundRobin);
        Assert.assertTrue(result.size() == (Test.NB_CONNECTED_ITFS + 1));
        for (int i = 0; i < result.size(); i++) {
            Assert.assertEquals(result.get(i).getIntValue(), expectedResults.get(i).getIntValue());
        }

        result = clientItf.testAllStdModes_Param(listParameter, listParameter, listParameter, listParameter,
                new WrappedInteger(42));
        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);

        result = clientItf.testCustom_Param(listForRoundRobin);
        // custom distrib discards some parameters
        Assert.assertTrue(result.size() == (Test.NB_CONNECTED_ITFS));
        //        Assert.assertTrue(result.size() == 2);
        for (int i = 0; i < result.size(); i++) {
            // custom distrib only returns first param of input list 
            Assert.assertEquals(listForRoundRobin.get(0).getIntValue(), result.get(i).getIntValue());
        }
        //        Assert.assertTrue(result.get(0).equals(listForRoundRobin.get(0)));

    }

    public void testOwnClientMulticastItf() {
        List<WrappedInteger> listParameter = new ArrayList<WrappedInteger>();
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            listParameter.add(i, new WrappedInteger(i));
        }
        List<WrappedInteger> result;

        result = multicastClientItf.testBroadcast_Param(listParameter);
        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            Assert.assertTrue(result.contains(new WrappedInteger(i))); // do
            // not
            // know
            // the
            // ordering
            // ...
            // ?
        }

        result = multicastClientItf.testBroadcast_Method(listParameter);
        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            Assert.assertTrue(result.contains(new WrappedInteger(i))); // do
            // not
            // know
            // the
            // ordering
            // ...
            // ?
        }

        result = multicastClientItf.testOneToOne_Param(listParameter);
        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            Assert.assertTrue(result.contains(new WrappedInteger(i))); // do
            // not
            // know
            // the
            // ordering
            // ...
            // ?
        }

        result = multicastClientItf.testOneToOne_Method(listParameter);
        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            Assert.assertTrue(result.get(i).equals(new WrappedInteger(i))); // do
            // not
            // know
            // the
            // ordering
            // ...
            // ?
        }

        List<WrappedInteger> listForRoundRobin = new ArrayList<WrappedInteger>();
        for (int i = 0; i < (Test.NB_CONNECTED_ITFS + 1); i++) {
            listForRoundRobin.add(i, new WrappedInteger(i));
        }
        //        result = multicastClientItf.testRoundRobin_Param(listForRoundRobin);
        //        Assert.assertTrue(result.size() == (Test.NB_CONNECTED_ITFS + 1));
        //
        //        result = multicastClientItf.testRoundRobin_Method(listForRoundRobin);
        //        Assert.assertTrue(result.size() == (Test.NB_CONNECTED_ITFS + 1));
        //
        //        result = multicastClientItf.testAllStdModes_Param(listParameter,
        //                listParameter, listParameter, listParameter,
        //                new WrappedInteger(42));
        //        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);

        //        result = multicastClientItf.testCustom_Param(listForRoundRobin);
        //        Assert.assertTrue(result.size() == 2);
        //        Assert.assertTrue(result.get(0).equals(listForRoundRobin.get(0)));

        result = multicastClientItf.testAllStdModes_Param(listParameter, listParameter, listParameter,
                listParameter, new WrappedInteger(42));
        Assert.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);

        result = multicastClientItf.testCustom_Param(listForRoundRobin);
        Assert.assertTrue(result.size() == 2);
        Assert.assertTrue(result.get(0).equals(listForRoundRobin.get(0)));

        result = multicastClientItf.testCustom_Method(listForRoundRobin);
        Assert.assertTrue(result.size() == 2);
        Assert.assertTrue(result.get(0).equals(listForRoundRobin.get(0)));
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
     *      java.lang.Object)
     */
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals("clientItf")) {
            clientItf = (MulticastTestItf) serverItf;
        } else if ("multicastClientItf".equals(clientItfName)) {
            multicastClientItf = (MulticastTestItf) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("clientItf".equals(clientItfName)) {
            return clientItf;
        }
        if ("multicastClientItf".equals(clientItfName)) {
            return multicastClientItf;
        }
        throw new NoSuchInterfaceException(clientItfName);
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        // TODO Auto-generated method stub
    }
}
