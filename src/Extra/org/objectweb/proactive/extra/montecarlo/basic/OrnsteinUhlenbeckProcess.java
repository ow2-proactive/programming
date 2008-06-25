package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.ExperienceSet;

import java.util.Random;


/**
 * OrnsteinUhlenbeckProcess : Generate scenarios that follow the Ornstein-Uhlenbeck process.
 *
 * @author The ProActive Team
 */
public class OrnsteinUhlenbeckProcess implements ExperienceSet {

    private int N;
    private double base;
    private double factor;

    /**
     * 
     * @param s0 The present value of the asset
     * @param mu Mean reversion level
     * @param lambda Mean reversion rate
     * @param sigma volatility
     * @param t time
     * @param n number of experiences
     */
    public OrnsteinUhlenbeckProcess(double s0, double mu, double lambda, double sigma, int t, int n) {
        N = n;
        base = s0 * Math.exp(-lambda * t) + mu * (1 - Math.exp(-lambda * t));
        factor = sigma * Math.sqrt((1 - Math.exp(-2 * lambda * t)) / (2 * lambda));

    }

    public double[] simulate(Random rng) {
        double[] answer = new double[N];
        for (int i = 0; i < N; i++) {
            answer[i] = base + factor * rng.nextGaussian();
        }

        return answer;
    }
}
