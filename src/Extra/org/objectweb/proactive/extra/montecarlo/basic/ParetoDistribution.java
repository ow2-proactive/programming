package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.ExperienceSet;

import java.util.Random;


/**
 * ParetoDistribution : This transform is used to generate Pareto distributed samples form uniform distributed samples.
 *
 * @author The ProActive Team
 */
public class ParetoDistribution implements ExperienceSet {

    private int N;
    private double alpha, beta;

    /**
     * This transform is used to generate Pareto distributed samples form uniform distributed samples.
     *
     * @param alpha Pareto distribution parameter
     * @param beta 	Pareto distribution parameter
     * @param n number of samples to generate

     */
    public ParetoDistribution(double beta, double alpha, int n) {
        this.beta = beta;
        N = n;
        this.alpha = alpha;
    }

    public double[] simulate(Random rng) {
        double[] answer = new double[N];
        for (int i = 0; i < N; i++) {
            answer[i] = beta / (-Math.pow(Math.log(rng.nextDouble()), 1 / alpha));
        }

        return answer;
    }
}
