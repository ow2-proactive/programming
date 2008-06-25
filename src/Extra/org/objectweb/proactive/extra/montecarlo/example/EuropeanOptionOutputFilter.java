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
package org.objectweb.proactive.extra.montecarlo.example;

import org.objectweb.proactive.extra.montecarlo.AbstractExperienceSetOutputFilter;
import org.objectweb.proactive.extra.montecarlo.basic.GeometricBrownianMotion;

import java.io.Serializable;


/**
 * EuropeanOptionOutputFilter
 *
 * @author The ProActive Team
 */
public class EuropeanOptionOutputFilter extends AbstractExperienceSetOutputFilter {
    private double strikePrice;

    public EuropeanOptionOutputFilter(GeometricBrownianMotion experienceSet, double strikePrice) {
        super(experienceSet);
        this.strikePrice = strikePrice;

    }

    public Serializable filter(Serializable experiencesResults) {

        double[] simulatedPrice = (double[]) experiencesResults;
        double payoffCall = 0, payoffPut = 0;
        for (int j = 0; j < simulatedPrice.length; j++) {
            payoffCall += Math.max(simulatedPrice[j] - this.strikePrice, 0);
            payoffPut += Math.max(this.strikePrice - simulatedPrice[j], 0);
        }
        return new PayOff(payoffCall, payoffPut);
    }

    public class PayOff implements Serializable {
        private double payoffCall;
        private double payoffPut;

        public PayOff(double payoffCall, double payoffPut) {
            this.payoffCall = payoffCall;
            this.payoffPut = payoffPut;
        }

        public double getPayoffCall() {
            return payoffCall;
        }

        public double getPayoffPut() {
            return payoffPut;
        }
    }
}
