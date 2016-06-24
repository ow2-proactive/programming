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
package dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectory;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ApplicationAlreadyRegisteredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceAlreadyRegisteredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.WrongApplicationIdException;


/**
 * SpacesDirectoryAbstractBase impl and additional NamingService tests.
 */
public class NamingServiceTest extends SpacesDirectoryAbstractBase {

    private NamingService ns;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ns = new NamingService();
    }

    @Test
    public void testIsApplicationRegistered1() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException {

        assertFalse(ns.isApplicationIdRegistered(MAIN_APPID));
        assertFalse(ns.isApplicationIdRegistered(ANOTHER_APPID1));

        ns.registerApplication(MAIN_APPID, null);

        assertFalse(ns.isApplicationIdRegistered(ANOTHER_APPID1));
        assertTrue(ns.isApplicationIdRegistered(MAIN_APPID));

        ns.unregisterApplication(MAIN_APPID);

        assertFalse(ns.isApplicationIdRegistered(MAIN_APPID));
        assertFalse(ns.isApplicationIdRegistered(ANOTHER_APPID1));
    }

    @Test
    public void testIsApplicationRegistered2() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException {

        Set<SpaceInstanceInfo> spaces = new HashSet<SpaceInstanceInfo>();

        spaces.add(spaceInstanceInput1);
        spaces.add(spaceInstanceInput2);
        spaces.add(spaceInstanceOutput1);
        spaces.add(spaceInstanceOutput2);

        assertFalse(ns.isApplicationIdRegistered(MAIN_APPID));
        assertFalse(ns.isApplicationIdRegistered(ANOTHER_APPID1));

        ns.registerApplication(MAIN_APPID, spaces);

        assertFalse(ns.isApplicationIdRegistered(ANOTHER_APPID1));
        assertTrue(ns.isApplicationIdRegistered(MAIN_APPID));

        ns.unregisterApplication(MAIN_APPID);

        assertFalse(ns.isApplicationIdRegistered(MAIN_APPID));
        assertFalse(ns.isApplicationIdRegistered(ANOTHER_APPID1));
    }

    @Test
    public void testRegisteredApplicationId1() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException {

        Set<String> apps;

        apps = ns.getRegisteredApplications();
        assertTrue(apps.isEmpty());

        ns.registerApplication(MAIN_APPID, null);
        apps = ns.getRegisteredApplications();
        assertFalse(apps.isEmpty());

        ns.unregisterApplication(MAIN_APPID);
        apps = ns.getRegisteredApplications();
        assertTrue(apps.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRegisteredApplicationId2() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException {

        Set<String> apps;
        ns.registerApplication(MAIN_APPID, null);
        apps = ns.getRegisteredApplications();
        apps.remove(MAIN_APPID);
    }

    /**
     * Normal case, two inputs, two outputs.
     */
    @Test
    public void testRegisterApplication1() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException, SpaceAlreadyRegisteredException, IllegalArgumentException {

        Set<SpaceInstanceInfo> spaces = new HashSet<SpaceInstanceInfo>();

        spaces.add(spaceInstanceInput1);
        spaces.add(spaceInstanceInput2);
        spaces.add(spaceInstanceOutput1);
        spaces.add(spaceInstanceOutput2);

        ns.registerApplication(MAIN_APPID, spaces);

        // check if everything has been registered
        assertIsSpaceRegistered(spaceInstanceInput1);
        assertIsSpaceRegistered(spaceInstanceInput2);
        assertIsSpaceRegistered(spaceInstanceOutput1);
        assertIsSpaceRegistered(spaceInstanceOutput2);

        assertTrue(ns.unregister(spaceInstanceInput1.getMountingPoint()));
        assertTrue(ns.unregister(spaceInstanceOutput1.getMountingPoint()));
        ns.register(spaceInstanceInput1);
        ns.register(spaceInstanceOutput1);
    }

    /**
     * Normal case, no spaces
     */
    @Test
    public void testRegisterApplication2() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException, SpaceAlreadyRegisteredException, IllegalArgumentException {

        ns.registerApplication(MAIN_APPID, null);
        ns.register(spaceInstanceInput1);
        ns.register(spaceInstanceOutput1);
        assertTrue(ns.unregister(spaceInstanceInput1.getMountingPoint()));
        assertTrue(ns.unregister(spaceInstanceOutput1.getMountingPoint()));
    }

    @Test
    public void testRegisterApplicationAlreadyRegistered() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException, SpaceAlreadyRegisteredException, IllegalArgumentException {

        ns.registerApplication(MAIN_APPID, null);

        try {
            ns.registerApplication(MAIN_APPID, null);
            fail("Exception expected");
        } catch (ApplicationAlreadyRegisteredException e) {
        } catch (Exception e) {
            fail("Exception of different type expected");
        }
    }

    @Test
    public void testRegisterApplicationWrongAppid() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException, SpaceAlreadyRegisteredException {

        Set<SpaceInstanceInfo> spaces = new HashSet<SpaceInstanceInfo>();

        spaces.add(spaceInstanceInput1);
        spaces.add(spaceInstanceInput2);
        spaces.add(spaceInstanceOutput1);
        spaces.add(spaceInstanceOutput2);

        try {
            ns.registerApplication(ANOTHER_APPID1, spaces);
            fail("Exception expected");
        } catch (WrongApplicationIdException e) {
        } catch (Exception e) {
            fail("Exception of different type expected");
        }
        // check not registered
        assertIsSpaceUnregistered(spaceInstanceInput1);
        assertIsSpaceUnregistered(spaceInstanceInput2);
        assertIsSpaceUnregistered(spaceInstanceOutput1);
        assertIsSpaceUnregistered(spaceInstanceOutput2);
        ns.registerApplication(ANOTHER_APPID1, null);
        ns.register(spaceInstanceInput1b);
        assertIsSpaceRegistered(spaceInstanceInput1b);
    }

    @Test
    public void testUnregisterApplication() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException {

        Set<SpaceInstanceInfo> spaces = new HashSet<SpaceInstanceInfo>();

        spaces.add(spaceInstanceInput1);
        spaces.add(spaceInstanceInput2);
        spaces.add(spaceInstanceOutput1);
        spaces.add(spaceInstanceOutput2);

        ns.registerApplication(MAIN_APPID, spaces);

        assertIsSpaceRegistered(spaceInstanceInput1);
        assertIsSpaceRegistered(spaceInstanceInput2);
        assertIsSpaceRegistered(spaceInstanceOutput1);
        assertIsSpaceRegistered(spaceInstanceOutput2);

        ns.unregisterApplication(MAIN_APPID);

        assertIsSpaceUnregistered(spaceInstanceInput1);
        assertIsSpaceUnregistered(spaceInstanceInput2);
        assertIsSpaceUnregistered(spaceInstanceOutput1);
        assertIsSpaceUnregistered(spaceInstanceOutput2);
    }

    @Test
    public void testUnregisterApplicationEmpty() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException {

        Set<SpaceInstanceInfo> spaces = new HashSet<SpaceInstanceInfo>();

        ns.registerApplication(MAIN_APPID, spaces);
        ns.unregisterApplication(MAIN_APPID);
    }

    @Test
    public void testUnregisterApplicationWrongAppid() throws ApplicationAlreadyRegisteredException,
            WrongApplicationIdException {

        ns.registerApplication(MAIN_APPID, null);

        try {
            ns.unregisterApplication(ANOTHER_APPID1);
            fail("Exception expected");
        } catch (WrongApplicationIdException e) {
        } catch (Exception e) {
            fail("Exception of different type expected");
        }
    }

    /**
     * Register space of not registered application
     */
    @Test
    public void testNSRegister1() throws ApplicationAlreadyRegisteredException,
            SpaceAlreadyRegisteredException, IllegalArgumentException, WrongApplicationIdException {

        try {
            ns.register(spaceInstanceInput1b);
            fail();
        } catch (WrongApplicationIdException e) {
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Unregister space of not registered application
     */
    @Test
    public void testNSUnregister1() throws ApplicationAlreadyRegisteredException,
            SpaceAlreadyRegisteredException, IllegalArgumentException, WrongApplicationIdException {

        assertFalse(ns.unregister(spaceInstanceInput1b.getMountingPoint()));
    }

    private void assertIsSpaceRegistered(SpaceInstanceInfo expected) {
        SpaceInstanceInfo actual = ns.lookupOne(expected.getMountingPoint());
        assertEquals(actual.getMountingPoint(), expected.getMountingPoint());
    }

    private void assertIsSpaceUnregistered(SpaceInstanceInfo expected) {
        assertNull(ns.lookupOne(expected.getMountingPoint()));
    }

    @Override
    protected SpacesDirectory getSource() throws Exception {
        NamingService ns = new NamingService();

        ns.registerApplication(MAIN_APPID, null);
        ns.registerApplication(ANOTHER_APPID1, null);
        ns.registerApplication(ANOTHER_APPID2, null);

        return ns;
    }
}
