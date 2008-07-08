/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.benchmarks.NAS.util;

import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;


public abstract class ReduceAll implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 40L;
    protected Object typedGroup;
    private int nbOperand;
    private int nbOperandServed;
    protected double sumDble;
    protected int[] tableI;
    protected double[] tableD;
    protected double maxDouble;

    /**
     * Don't use
     */
    public ReduceAll() {
    }

    public ReduceAll(Object g, int n) { // compute n or keep arg?
        this.typedGroup = g;
        this.nbOperand = n;
        this.reset();
    }

    public void reinitialize(Object g, int n) {
        this.typedGroup = g;
        this.nbOperand = n;
        this.reset();
    }

    /**
     * Reset all fields using for computation
     */
    public void reset() {
        this.nbOperandServed = 0;
        this.tableI = null;
        this.tableD = null;
        this.sumDble = 0.;
        this.maxDouble = 0.;
    }

    /**
     * Reset all fields using for computation and data on the SPMG group
     *
     */
    public void resetAll() {
        this.typedGroup = null;
        this.nbOperand = 0;
        this.reset();
    }

    /**
     * Compute the sum of n operands and send it to the SPMD group
     *
     * @param tab one operand of the sum operation
     */
    public void sum(int[] tab) {
        if (this.tableI == null) {
            this.tableI = new int[tab.length];
        } else if (this.tableI.length != tab.length) {
            throw new RuntimeException();
        }

        for (int i = 0; i < tab.length; i++) {
            this.tableI[i] += tab[i];
        }

        this.nbOperandServed++;

        if (this.nbOperand == this.nbOperandServed) {
            this.send();
        }
    }

    /**
     * Compute the sum of n operands and send it to the SPMD group
     *
     * @param tab one operand of the sum operation
     */
    public void sum(double[] tab) {
        if (this.tableD == null) {
            this.tableD = new double[tab.length];
        } else if (this.tableD.length != tab.length) {
            throw new RuntimeException();
        }

        for (int i = 0; i < tab.length; i++) {
            this.tableD[i] += tab[i];
        }

        this.nbOperandServed++;

        if (this.nbOperand == this.nbOperandServed) {
            this.send();
        }
    }

    public void sum(double d) {
        this.sumDble += d;

        this.nbOperandServed++;
        if (this.nbOperand == this.nbOperandServed) {
            this.send();
        }
    }

    public void max(double i) {
        if (i > this.maxDouble) {
            this.maxDouble = i;
        }

        this.nbOperandServed++;
        if (this.nbOperand == this.nbOperandServed) {
            this.send();
        }
    }

    /**
     * Implement this for recover the result by call an method. One
     * operation in the same time. Acces to the result by the appropriate
     * protected field.
     */
    protected abstract void send();

    /**
     * Kill this active object.
     *
     */
    public void destroy() {
        PAActiveObject.getBodyOnThis().terminate();
    }
}
