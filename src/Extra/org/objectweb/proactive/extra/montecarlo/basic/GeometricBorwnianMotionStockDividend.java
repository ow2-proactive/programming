package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.ExperienceSet;

import java.util.Random;


/**
 * GeometricBorwnianMotionStockDividend : Simulating geometric Brownian motion with a stock dividend.
 *
 * @author The ProActive Team
 */
public class GeometricBorwnianMotionStockDividend implements ExperienceSet {

    private double s0, Y, r, sigma, D;
    private int T, N;

    /**
     * Simulating geometric Brownian motion with a stock dividend
     *
     * @param s0 Initial value at t=0 of geometric Brownian
     * @param Y Yield of the underlying, for stocks Y=r (interest rate), futures Y=0, currencies Y=(domestic interest rate-foreign interest rate)
     * @param r Continuous compounded interest rate
     * @param sigma Volatility
     * @param D Stock dividend percentage
     * @param T final time
     * @param N number of experiences
     */
    public GeometricBorwnianMotionStockDividend(double s0, double Y, double r, double sigma, double D, int T,
            int N) {
        this.s0 = s0;
        this.Y = Y;
        this.r = r;
        this.sigma = sigma;
        this.D = D;
        this.T = T;
        this.N = N;
    }

    public double[] simulate(Random rng) {
        double[] answer = new double[N];
        for (int i = 0; i < N; i++) {
            answer[i] = (s0 / (1 + D)) *
                Math.exp((Y - 0.5 * sigma * sigma) * T + sigma * Math.sqrt(T) * rng.nextGaussian());
        }
        return answer;
    }
}