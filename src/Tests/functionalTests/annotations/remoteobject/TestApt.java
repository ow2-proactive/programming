/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.annotations.remoteobject;

import junit.framework.Assert;
import functionalTests.annotations.AptTest;


public class TestApt extends AptTest {

    @org.junit.Test
    public void action() throws Exception {
        // misplaced annotation
        Assert.assertEquals(ERROR, checkFile("MisplacedAnnotation"));

        // basic checks
        Assert.assertEquals(new Result(0, 6), checkFile("WarningGettersSetters"));
        Assert.assertEquals(ERROR, checkFile("ErrorFinalClass"));
        Assert.assertEquals(new Result(2, 0), checkFile("ErrorFinalMethods"));
        Assert.assertEquals(ERROR, checkFile("ErrorNoArgConstructor"));
        Assert.assertEquals(ERROR, checkFile("ErrorClassNotPublic"));
        Assert.assertEquals(ERROR, checkFile("PrivateEmptyConstructor"));
        Assert.assertEquals(OK, checkFile("EmptyConstructor"));

        // more complicated scenarios
        Assert.assertEquals(WARNING, checkFile("ErrorReturnTypes")); // because of getter/setter
        Assert.assertEquals(new Result(1, 1), checkFile("Reject"));
        Assert.assertEquals(OK, checkFile("CorrectedReject"));

        Assert.assertEquals(ERROR, checkFile("ErrorEmptyConstructor"));

    }
}
