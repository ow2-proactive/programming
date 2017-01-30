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
package functionalTests.annotations.remoteobject;

import org.junit.Assert;

import functionalTests.annotations.AnnotationTest.Result;
import functionalTests.annotations.CTreeTest;


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

        // more complicated scenarios
        Assert.assertEquals(WARNING, checkFile("ErrorReturnTypes")); // because of getter/setter
        Assert.assertEquals(new Result(1, 1), checkFile("Reject"));
        Assert.assertEquals(OK, checkFile("CorrectedReject"));

        // CTREE - specific
        Assert.assertEquals(OK, checkFile("ErrorReturnsNull"));
        Assert.assertEquals(new Result(0, 1), checkFile("ErrorNonEmptyConstructor"));
        Assert.assertEquals(OK, checkFile("NoConstructor"));
    }
}
