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
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.annotations.migrationsignal;

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

        // misplaced annotation
        Assert.assertEquals(ERROR, checkFile("MisplacedAnnotation"));

        // checking conditions that should be ok
        Assert.assertEquals(ERROR, checkFile("AcceptSimple"));
        Assert.assertEquals(OK, checkFile("AcceptIndirectCall"));
        Assert.assertEquals(OK, checkFiles("inter.MigrationProvider", "AcceptInterClassCall")); // the order counts!
        Assert.assertEquals(OK, checkFile("AcceptInnerInterClassCall"));

        // checking conditions that should be seen as errors
        //      NOTA can be outside active object  Assert.assertEquals(ERROR, checkFile("ErrorNotInActiveObject"));
        Assert.assertEquals(new Result(4, 0), checkFile("ErrorNotLast"));

        Assert.assertEquals(ERROR, checkFile("ErrorNoMigrateTo"));

        // block checking  
        Assert.assertEquals(new Result(2, 0), checkFile("ErrorNotLastBlock"));
        Assert.assertEquals(OK, checkFile("AcceptBlock")); // for sickos
        Assert.assertEquals(ERROR, checkFile("TryCatchFinally"));
        Assert.assertEquals(OK, checkFile("CatchMigrationException"));
        Assert.assertEquals(new Result(2, 0), checkFile("Fi"));
        Assert.assertEquals(new Result(2, 0), checkFile("Loopz")); // loopz are good!
        Assert.assertEquals(OK, checkFile("Synchronized")); // for sickos
        Assert.assertEquals(ERROR, checkFile("Switch"));

    }
}
