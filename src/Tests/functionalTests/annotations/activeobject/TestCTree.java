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
package functionalTests.annotations.activeobject;

import junit.framework.Assert;
import functionalTests.annotations.CTreeTest;


/**
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class TestCTree extends CTreeTest {

    @org.junit.Test
    public void action() throws Exception {

        // TODO how to refactor? - checkFile() has different implementation for apt and ctree tests
        // APT
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
        Assert.assertEquals(ERROR, checkFile("InnerClasses"));

        // more complicated scenarios
        Assert.assertEquals(WARNING, checkFile("ErrorReturnTypes")); // because of getter/setter
        Assert.assertEquals(new Result(1, 1), checkFile("Reject"));
        Assert.assertEquals(OK, checkFile("CorrectedReject"));

        // CTREE - specific
        Assert.assertEquals(new Result(0, 2), checkFile("ErrorReturnsNull"));
        Assert.assertEquals(new Result(0, 1), checkFile("ErrorNonEmptyConstructor"));
        Assert.assertEquals(OK, checkFile("NoConstructor"));
    }
}
