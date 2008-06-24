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

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extra.montecarlo.EngineTask;
import org.objectweb.proactive.extra.montecarlo.Executor;
import org.objectweb.proactive.extra.montecarlo.ExperienceSet;
import org.objectweb.proactive.extra.montecarlo.PAMonteCarlo;
import org.objectweb.proactive.extra.montecarlo.Simulator;
import org.objectweb.proactive.extra.montecarlo.basic.GeometricBrownianMotion;
import org.objectweb.proactive.api.PALifeCycle;


public class EuropeanOption implements EngineTask {

    private double spotPrice, strikePrice, dividend, interestRate, volatilityRate, maturityDate;
    private int N, M;

    /**
     * @param spotPrice
     * @param strikePrice
     * @param dividend
     * @param interestRate
     * @param volatilityRate
     * @param maturityDate
     * @param n
     * @param m
     */
    public EuropeanOption(double spotPrice, double strikePrice, double dividend, double interestRate,
            double volatilityRate, double maturityDate, int n, int m) {
        super();
        this.spotPrice = spotPrice;
        this.strikePrice = strikePrice;
        this.dividend = dividend;
        this.interestRate = interestRate;
        this.volatilityRate = volatilityRate;
        this.maturityDate = maturityDate;
        N = n;
        M = m;
    }

    public Serializable run(Simulator simulator, Executor executor) {
        List<ExperienceSet> sets = new ArrayList<ExperienceSet>(M);
        for (int i = 0; i < M; i++) {
            sets.add(new GeometricBrownianMotion(spotPrice, interestRate, volatilityRate, maturityDate, N));
        }
        Enumeration<double[]> simulatedPriceList = null;
        try {
            simulatedPriceList = simulator.solve(sets);
        } catch (TaskException e) {
            throw new RuntimeException(e);
        }

        double payoffCall = 0;
        double payoffPut = 0;
        int counter = 1;
        while (simulatedPriceList.hasMoreElements()) {
            double[] simulatedPrice = simulatedPriceList.nextElement();
            for (int j = 0; j < N; j++) {
                payoffCall += Math.max(simulatedPrice[j] - this.strikePrice, 0);
                payoffPut += Math.max(this.strikePrice - simulatedPrice[j], 0);
            }
        }

        double call, put;

        call = payoffCall * Math.exp(-this.maturityDate * this.interestRate) / (N * M);
        put = payoffPut * Math.exp(-this.maturityDate * this.interestRate) / (N * M);

        return new double[] { call, put };
    }

    public static void main(String[] args) throws ProActiveException, TaskException {
        URL descriptor = EuropeanOption.class.getResource("WorkersApplication.xml");
        PAMonteCarlo mc = new PAMonteCarlo(descriptor, null, "Workers");

        EuropeanOption option = new EuropeanOption(100.0, 100.0, 0.1, 0.05, 0.2, 1, 10000, 1000);
        double[] price = (double[]) mc.run(option);
        System.out.println("Call: " + price[0] + " Put : " + price[1]);
        mc.terminate();
        PALifeCycle.exitSuccess();

    }

}
