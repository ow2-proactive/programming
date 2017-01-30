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
package functionalTests;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.utils.OperatingSystem;


/**
 * Helper class to disable test when certain condition are meet.
 *
 * Junit does not support group of tests. There is no straightforward way
 * to disable a broken or platform specific test. It is also hard to create
 * custom annotations/runner to tag test with metadata.
 *
 * Methods of this class should be called in {@link BeforeClass} or {@link Before}
 *
 * @author ProActive team
 * @since  ProActive 5.2.0
 */
public class TestDisabler {

    private TestDisabler() {

    }

    /**
     * Indicates on which platform the test must be run.
     *
     * @param oses
     *    An array of operating system
     */
    static public void supportedOs(OperatingSystem... oses) {
        OperatingSystem localOs = OperatingSystem.getOperatingSystem();
        for (OperatingSystem os : oses) {
            if (localOs == os) {
                return;
            }
        }

        log("Test do not support " + localOs);
        Assume.assumeTrue(false);
    }

    /**
     * Indicates on which platform the test must be disabled.
     *
     * @param oses
     *    An array of operating system
     */
    static public void unsupportedOs(OperatingSystem... oses) {
        OperatingSystem localOs = OperatingSystem.getOperatingSystem();
        for (OperatingSystem os : oses) {
            if (localOs == os) {
                log("Test must not be run on " + localOs);
                Assume.assumeTrue(false);
            }
        }

    }

    /**
     * Indicates with which protocols the test must be enabled
     * 
     * @param protocols
     *    An array of communication protocols
     */
    static public void supportedProtocols(String... protocols) {
        String localProtocol;
        try {
            localProtocol = AbstractRemoteObjectFactory.getDefaultRemoteObjectFactory().getProtocolId();
            for (String protocol : protocols) {
                if (localProtocol.equals(protocol)) {
                    return;
                }
            }

            log("Test do not support " + localProtocol);
            Assume.assumeTrue(false);
        } catch (UnknownProtocolException e) {
            // run it
        }
    }

    /**
     * Indicates with which protocols the test must be disabled
     * 
     * @param protocols
     *    An array of communication protocols
     */
    static public void unsupportedProtocols(String... protocols) {
        String localProtocol;
        try {
            localProtocol = AbstractRemoteObjectFactory.getDefaultRemoteObjectFactory().getProtocolId();
            for (String protocol : protocols) {
                if (localProtocol.equals(protocol)) {
                    log("Test must not be run with " + localProtocol);
                    Assume.assumeTrue(false);
                }
            }
        } catch (UnknownProtocolException e) {
            // run it
        }
    }

    /**
     * Indicates that the test is unstable for an unknown reason and should not be run.
     * 
     * The feature, the test or both can be broken.
     */
    static public void unstable() {
        // Disable unstable tests
        log("Test is unstable for an unknown reason");
        Assume.assumeTrue(false);
    }

    /**
     * Indicate that the tested feature is broken and the should not be run.
     * 
     * The test is supposed to be correct. 
     */
    static public void waitingFeatureFix() {
        log("Feautre must be fixed");
        Assume.assumeTrue(false);
    }

    /**
     * Indicate that the test is broken and should not be run.
     * 
     * The feature is supposed to be working correctly.
     */
    static public void waitingTestFix() {
        log("Test must be fixed");
        Assume.assumeTrue(false);
    }

    private static void log(String cause) {
        FunctionalTest.logger.info("Disabled test: " + cause);
    }

}
