package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.ExperienceSet;

import java.util.Random;


/**
 * ExponentialDistribution
 *
 * @author The ProActive Team
 */
public class ExponentialDistribution implements ExperienceSet {

    private double lambda;
    private int N;

    /**
     * Generates exponential distributed samples from a uniform distributed variable.
     *
     * @param lambda Exponential distribution parameter
     */
    public ExponentialDistribution(double lambda, int N) {
        this.lambda = lambda;
        this.N = N;
    }

    public double[] simulate(Random rng) {
        double[] answer = new double[N];
        for (int i = 0; i < N; i++) {
            answer[i] = -lambda * Math.log(rng.nextDouble());
        }

        return answer;
    }
}
