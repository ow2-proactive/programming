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
package org.objectweb.proactive.examples.matrix;

import java.util.List;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class Launcher implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private List<Node> nodesList;

    public Launcher() {
    }

    public Launcher(List<Node> nodesList) throws NodeException {
        this.nodesList = nodesList;
    }

    // MAIN !!!
    public void start(Matrix m1, Matrix m2, int i) {
        // DISTRIBUTED MULTIPLICATION      
        int matrixSize = m1.getWidth();

        long startTime;
        long endTime;

        startTime = System.currentTimeMillis();

        //System.out.println("Multiplication!!!!! ");
        Matrix groupResult = multiply(m1, m2 /* group */);

        //endTime = System.currentTimeMillis() - startTime;
        //System.out.println("     Distributed Multiplication : " + endTime + " ms\n");
        //startTime = System.currentTimeMillis();
        // RECONSTRUCTION
        try {
            Matrix result = reconstruction(groupResult, matrixSize);
        } catch (Exception e) {
        }

        endTime = System.currentTimeMillis() - startTime;
        logger.info("\n       Result (" + i + ") : Total time spent = " + endTime + " millisecondes");

        //System.out.println(result);
    }

    public Matrix createMatrix(int size) {
        Matrix m = new Matrix(size, size);
        m.initializeWithRandomValues();
        return m;
    }

    public Matrix distribute(Matrix m) {
        Matrix verticalSubMatrixGroup = null;
        Node[] dummy = {};
        Node[] nodesArray = nodesList.toArray(dummy);
        verticalSubMatrixGroup = m.transformIntoActiveVerticalSubMatrixGroup(nodesArray);

        return verticalSubMatrixGroup;
    }

    public Matrix multiply(Matrix m, Matrix group) {
        Matrix ma = group.localMultiplyForGroup(m);
        return ma;
    }

    public Matrix reconstruction(Matrix group, int size) {
        Matrix result = null;

        result = new Matrix(group, size);

        return result;
    }

    public String getString(Matrix m) {
        return m.toString();
    }
}
