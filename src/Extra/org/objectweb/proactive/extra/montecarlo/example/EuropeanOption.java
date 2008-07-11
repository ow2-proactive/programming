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

import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extra.montecarlo.AbstractExperienceSetPostProcess;
import org.objectweb.proactive.extra.montecarlo.EngineTask;
import org.objectweb.proactive.extra.montecarlo.Executor;
import org.objectweb.proactive.extra.montecarlo.ExperienceSet;
import org.objectweb.proactive.extra.montecarlo.PAMonteCarlo;
import org.objectweb.proactive.extra.montecarlo.Simulator;
import org.objectweb.proactive.extra.montecarlo.basic.GeometricBrownianMotion;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class EuropeanOption implements EngineTask<double[]> {

    /**
     * 
     */
    private static final long serialVersionUID = 40L;

    private double spotPrice, strikePrice, dividend, interestRate, volatilityRate, maturityDate;

    private int N, M;

    /**
     * @param spotPrice
     *            The initial underlying asset price at the start time of the
     *            option contract
     * @param strikePrice
     *            The price to exercise the option contract
     * @param dividend
     *            Dividend rate of the underlying asset
     * @param interestRate
     *            Constant interest rate
     * @param volatilityRate
     *            Volatility rate of the underlying asset
     * @param maturityDate
     *            The maturity date of the option contract
     * @param n
     *            number of simulations per task
     * @param m
     *            total number of simulations
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

    /**
     * Top-level task submits standard Monte-Carlo European option pricing
     * experiences.
     * 
     * @param simulator
     * @param executor
     * @return option price
     */
    public double[] run(Simulator simulator, Executor executor) {
        List<ExperienceSet<double[]>> sets = new ArrayList<ExperienceSet<double[]>>(M);
        // Simulate M Monte Carlo simulations to estimate M underlying asset
        // prices at the maturity date.
        for (int i = 0; i < M; i++) {
            sets.add(new AbstractExperienceSetPostProcess<double[], double[]>(new GeometricBrownianMotion(
                spotPrice, interestRate, volatilityRate, maturityDate, N)) {
                /**
                     * 
                     */
                private static final long serialVersionUID = 40L;

                // Compute the payoff of both call [index 1] and put [index 0]
                // options
                public double[] postprocess(double[] experiencesResults) {
                    double[] simulatedPrice = experiencesResults;
                    double[] payoff = new double[] { 0, 0 };
                    for (int j = 0; j < simulatedPrice.length; j++) {
                        payoff[0] += Math.max(simulatedPrice[j] - strikePrice, 0);
                        payoff[1] += Math.max(strikePrice - simulatedPrice[j], 0);
                    }
                    return payoff;
                }
            });
        }
        Enumeration<double[]> simulatedPriceList = null;
        // Submitting these experience sets to the master
        try {
            simulatedPriceList = simulator.solve(sets);
        } catch (TaskException e) {
            throw new RuntimeException(e);
        }
        // To estimate the expectation of both discount call and put payoffs
        // we iterates over the results, the number of successful experiences
        // are accumulated to get the sum of call and put payoff
        double payoffCall = 0;
        double payoffPut = 0;
        while (simulatedPriceList.hasMoreElements()) {
            double[] simulatedPayOff = simulatedPriceList.nextElement();
            payoffCall += simulatedPayOff[0];
            payoffPut += simulatedPayOff[1];
        }
        double call, put;
        // Discount both call and put payoffs then do the average
        call = payoffCall * Math.exp(-this.maturityDate * this.interestRate) / (N * M);
        put = payoffPut * Math.exp(-this.maturityDate * this.interestRate) / (N * M);
        // to return the call and put option prices
        return new double[] { call, put };
    }

    public static void main(String[] args) throws ProActiveException, TaskException {

        findOS();
        // Get the descriptor
        URL descriptor = EuropeanOption.class.getResource("WorkersApplication.xml");
        // Initialize the framework
        PAMonteCarlo<double[]> mc = new PAMonteCarlo<double[]>(descriptor, null, "Workers");
        // Initialize the top-level task
        EuropeanOption option = new EuropeanOption(100.0, 100.0, 0.1, 0.05, 0.2, 1, 10000, 1000);
        // Starts the top-level task
        double[] price = mc.run(option);
        System.out.println("Call: " + price[0] + " Put : " + price[1]);
        mc.terminate();
        PALifeCycle.exitSuccess();

    }

    public static void findOS() {
        // Finding current os
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("windows")) {
            System.setProperty("os", "windows");
        } else {
            System.setProperty("os", "unix");
        }
    }

}
