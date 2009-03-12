/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.examples.scilab.test;

import java.util.List;

import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.examples.scilab.AbstractData;
import org.objectweb.proactive.examples.scilab.GeneralResult;
import org.objectweb.proactive.examples.scilab.MSDeployEngine;
import org.objectweb.proactive.examples.scilab.MSEngine;
import org.objectweb.proactive.examples.scilab.SciTask;


public class SciTest1 {
    public static void main(String[] args) throws Exception {
        //initialized dispatcher engine
        SciData m1 = new SciDoubleMatrix("a", 1, 1, new double[] { 15 });
        SciData m2 = new SciDoubleMatrix("b", 1, 1, new double[] { 23 });
        SciData m3 = new SciDoubleMatrix("x", 1, 1);

        SciTask task = new SciTask("id");
        task.addDataIn(m1);
        task.addDataIn(m2);
        task.addDataIn(m3);
        task.addDataOut("x");
        task.setJob("x = a+b;");

        // local deployment
        MSEngine engine = MSDeployEngine.deploy("ScilabEngine");
        BooleanWrapper isActivate = engine.activate();

        if (isActivate.booleanValue()) {
            System.out.println("->Scilab engine is not activate");
        }

        GeneralResult sciResult = engine.execute(task);
        List<AbstractData> listResult = sciResult.getList();

        for (AbstractData data : listResult) {
            System.out.println(data);
        }

        engine.exit();
        System.exit(0);
    }
}
