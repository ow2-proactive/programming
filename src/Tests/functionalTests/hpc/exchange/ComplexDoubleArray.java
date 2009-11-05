/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
//@snippet-start Exchange_2
package functionalTests.hpc.exchange;

import org.objectweb.proactive.ext.hpc.exchange.ExchangeableDouble;


/**
 * Implementation example of an {@link ExchangeableDouble} used by the exchange operation.
 */
public class ComplexDoubleArray implements ExchangeableDouble {
    private double[] array;
    private int getPos, putPos;

    public ComplexDoubleArray(int size, boolean odd) {
        this.array = new double[size];
        this.getPos = odd ? 1 : 0;
        this.putPos = odd ? 0 : 1;
        for (int i = getPos; i < array.length; i += 2) {
            array[i] = Math.random();
        }
    }

    public double getChecksum() {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    public String toString() {
        return java.util.Arrays.toString(array);
    }

    public double get() {
        double res = array[getPos];
        getPos += 2;
        return res;
    }

    public boolean hasNextGet() {
        return getPos < array.length;
    }

    public boolean hasNextPut() {
        return putPos < array.length;
    }

    public void put(double value) {
        array[putPos] = value;
        putPos += 2;
    }
}
//@snippet-end Exchange_2