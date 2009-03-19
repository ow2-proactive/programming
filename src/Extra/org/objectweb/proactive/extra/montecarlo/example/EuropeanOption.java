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
package org.objectweb.proactive.extra.montecarlo.example;

import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extra.montecarlo.AbstractSimulationSetPostProcess;
import org.objectweb.proactive.extra.montecarlo.SimulationSet;
import org.objectweb.proactive.extra.montecarlo.*;
import org.objectweb.proactive.extra.montecarlo.basic.GeometricBrownianMotion;
import org.apache.commons.cli.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.io.File;


public class EuropeanOption implements EngineTask<double[]> {

    public static final String DEFAULT_DESCRIPTOR = "WorkersApplication.xml";
    public static final String DEFAULT_WORKERS_NAME = "Workers";
    public static final double DEFAULT_SPOT_PRICE = 100.0;
    public static final double DEFAULT_STRIKEPRICE = 100.0;
    public static final double DEFAULT_DIVIDEND = 0.1;
    public static final double DEFAULT_INTERESTRATE = 0.05;
    public static final double DEFAULT_VOLATILITYRATE = 0.2;
    public static final double DEFAULT_MATURITYDATE = 1;
    public static final int DEFAULT_NB_SIM = 10000;
    public static final int DEFAULT_NB_ITER = 1000;

    private static URL descriptor_url;
    private static String vn_name;
    private static String master_vn_name;

    private static double spotPrice, strikePrice, dividend, interestRate, volatilityRate, maturityDate;

    private static int nb_sim, nb_iter;

    private double spot, strike, divid, interest, volatility, maturity;

    private int N, M;

    public EuropeanOption(double spotPrice, double strikePrice, double dividend, double interestRate,
            double volatilityRate, double maturityDate, int nb_sim, int nb_iter) {
        super();
        this.spot = spotPrice;
        this.strike = strikePrice;
        this.divid = dividend;
        this.interest = interestRate;
        this.volatility = volatilityRate;
        this.maturity = maturityDate;
        this.N = nb_sim;
        this.M = nb_iter;
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
        List<SimulationSet<double[]>> sets = new ArrayList<SimulationSet<double[]>>(M);
        // Simulate M Monte Carlo simulations to estimate M underlying asset
        // prices at the maturity date.
        for (int i = 0; i < M; i++) {
            sets.add(new AbstractSimulationSetPostProcess<double[], double[]>(new GeometricBrownianMotion(
                spot, interest, volatility, maturity, N)) {
                // Compute the payoff of both call [index 1] and put [index 0]
                // options
                public double[] postprocess(double[] experiencesResults) {
                    double[] simulatedPrice = experiencesResults;
                    double[] payoff = new double[] { 0, 0 };
                    for (int j = 0; j < simulatedPrice.length; j++) {
                        payoff[0] += Math.max(simulatedPrice[j] - strike, 0);
                        payoff[1] += Math.max(strike - simulatedPrice[j], 0);
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
        call = payoffCall * Math.exp(-this.maturity * this.interest) / (N * M);
        put = payoffPut * Math.exp(-this.maturity * this.interest) / (N * M);
        // to return the call and put option prices
        return new double[] { call, put };
    }

    /**
     * Init the example with command line arguments
     * @param args
     * @throws java.net.MalformedURLException
     */
    public static void init(String[] args) throws MalformedURLException {

        Options command_options = new Options();
        command_options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription(
                "descriptor in use").create("d"));
        command_options.addOption(OptionBuilder.withArgName("name").hasArg().withDescription(
                "workers virtual node name").create("w"));
        command_options.addOption(OptionBuilder.withArgName("name").hasArg().withDescription(
                "master virtual node name").create("m"));
        command_options
                .addOption(OptionBuilder.withArgName("value").hasArg().withDescription(
                        "The initial underlying asset price at the start time of the option contract")
                        .create("spot"));
        command_options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription(
                "The price to exercise the option contract").create("strike"));
        command_options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription(
                "Dividend rate of the underlying asset").create("dividend"));
        command_options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription(
                "Constant interest rate").create("interest"));
        command_options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription(
                "Volatility rate of the underlying asset").create("volatility"));
        command_options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription(
                "The maturity date of the option contract").create("maturity"));
        command_options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription(
                "number of Monte-Carlo experience on each iteration").create("e"));
        command_options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription(
                "number of iterations").create("i"));

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("EuropeanOption", command_options);

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(command_options, args);
        } catch (ParseException e) {
            System.err.println("Parsing failed, reason, " + e.getMessage());
            System.exit(1);
        }

        // get descriptor option value
        String descPath = cmd.getOptionValue("d");

        if (descPath == null) {
            descriptor_url = PiMonteCarlo.class.getResource(DEFAULT_DESCRIPTOR);
            if (descriptor_url == null) {
                System.err.println("Couldn't find internal ressource: " + DEFAULT_DESCRIPTOR);
                System.exit(1);
            }
        } else {
            // check provided descriptor
            File descriptorFile = new File(descPath);
            if (!descriptorFile.exists()) {
                System.err.println("" + descriptorFile + " does not exist");
                System.exit(1);
            } else if (!descriptorFile.canRead()) {
                System.err.println("" + descriptorFile + " can't be read");
                System.exit(1);
            } else if (!descriptorFile.isFile()) {
                System.err.println("" + descriptorFile + " is not a regular file");
                System.exit(1);
            }
            descriptor_url = descriptorFile.toURI().toURL();
        }

        // get vn option value
        vn_name = cmd.getOptionValue("w");
        if (vn_name == null) {
            vn_name = DEFAULT_WORKERS_NAME;
        }

        master_vn_name = cmd.getOptionValue("m");

        String spot_string = cmd.getOptionValue("spot");
        if (spot_string == null) {
            spotPrice = DEFAULT_SPOT_PRICE;
        } else {
            spotPrice = Double.parseDouble(spot_string);
        }

        String strike_string = cmd.getOptionValue("strike");
        if (strike_string == null) {
            strikePrice = DEFAULT_STRIKEPRICE;
        } else {
            strikePrice = Double.parseDouble(strike_string);
        }
        String dividend_string = cmd.getOptionValue("dividend");
        if (dividend_string == null) {
            dividend = DEFAULT_DIVIDEND;
        } else {
            dividend = Double.parseDouble(dividend_string);
        }

        String interest_string = cmd.getOptionValue("interest");
        if (interest_string == null) {
            interestRate = DEFAULT_INTERESTRATE;
        } else {
            interestRate = Double.parseDouble(interest_string);
        }

        String volatility_string = cmd.getOptionValue("volatility");
        if (volatility_string == null) {
            volatilityRate = DEFAULT_VOLATILITYRATE;
        } else {
            volatilityRate = Double.parseDouble(volatility_string);
        }

        String maturity_string = cmd.getOptionValue("maturity");
        if (maturity_string == null) {
            maturityDate = DEFAULT_MATURITYDATE;
        } else {
            maturityDate = Double.parseDouble(maturity_string);
        }

        String nsim_string = cmd.getOptionValue("e");
        if (nsim_string == null) {
            nb_sim = DEFAULT_NB_SIM;
        } else {
            nb_sim = Integer.parseInt(nsim_string);
        }

        String ntasks_string = cmd.getOptionValue("i");
        if (ntasks_string == null) {
            nb_iter = DEFAULT_NB_ITER;
        } else {
            nb_iter = Integer.parseInt(ntasks_string);
        }

    }

    public static void main(String[] args) throws ProActiveException, TaskException, MalformedURLException {

        init(args);
        // Initialize the framework
        final PAMonteCarlo<double[]> mc = new PAMonteCarlo<double[]>(descriptor_url, master_vn_name, vn_name);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                mc.terminate();
            }
        }));

        // Initialize the top-level task
        EuropeanOption option = new EuropeanOption(spotPrice, strikePrice, dividend, interestRate,
            volatilityRate, maturityDate, nb_sim, nb_iter);
        // Starts the top-level task
        double[] price = mc.run(option);
        System.out.println("European Option simulation finished with the following parameters:");
        System.out.println("spotPrice = " + spotPrice);
        System.out.println("strikePrice = " + strikePrice);
        System.out.println("dividend = " + dividend);
        System.out.println("interestRate = " + interestRate);
        System.out.println("volatilityRate = " + volatilityRate);
        System.out.println("maturityDate = " + maturityDate);
        System.out.println();
        System.out.println("Result of the simulation:");
        System.out.println("Call = " + price[0] + " Put = " + price[1]);

        PALifeCycle.exitSuccess();

    }

}
