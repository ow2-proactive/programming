/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


/**
 * TestAggreagateException
 *
 * @author The ProActive Team
 **/
public class TestAggregateException {

    @Test
    public void testPrintStack() {
        int nbEx = 3;
        String CAUSEPATTERN = "CAUSE";
        String SUBCAUSEPATTERN = "SUBCAUSE";
        String MESSAGE = "MESSAGE";
        List<Throwable> list1 = new ArrayList<Throwable>();
        for (int i = 0; i < nbEx; i++) {
            List<Throwable> list2 = new ArrayList<Throwable>();

            for (int j = 0; j < nbEx; j++) {
                list2.add(new Exception(SUBCAUSEPATTERN + "_" + i + "_" + j));
            }
            AggregateException aggsub = new AggregateException(CAUSEPATTERN + "_" + i, list2);
            list1.add(aggsub);

        }
        AggregateException agg = new AggregateException(MESSAGE, list1);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        agg.printStackTrace(pw);

        String printedStack = sw.toString();

        // Prints on stderr for logging purpose
        agg.printStackTrace();

        Assert.assertTrue(printedStack.contains(MESSAGE));

        for (int i = 0; i < nbEx; i++) {
            Assert.assertTrue(printedStack.contains(CAUSEPATTERN + "_" + i));
            for (int j = 0; j < nbEx; j++) {
                Assert.assertTrue(printedStack.contains(SUBCAUSEPATTERN + "_" + i + "_" + j));
            }
        }

    }
}
