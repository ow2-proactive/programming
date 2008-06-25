package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.ExperienceSet;

import java.util.Random;


/**
 * GeometricBorwnianMotionCashDividend : Simulating geometric Brownian motion with a cash dividend before T
 *
 * @author The ProActive Team
 */
public class GeometricBorwnianMotionMultipleCashDividends implements ExperienceSet {

    private double Y, sigma;
    private double T;
    private int N;

    private double alpha;

    /**
     * Simulating geometric Brownian motion with a cash dividend before T
     *
     * @param s0 Initial value at t=0 of geometric Brownian
     * @param Y Yield of the underlying, for stocks Y=r (interest rate), futures Y=0, currencies Y=(domestic interest rate-foreign interest rate)
     * @param r Continuous compounded interest rate
     * @param sigma Volatility
     * @param Dti Cash dividends amount at time ti
     * @param ti time of the cash dividend
     * @param T final time
     * @param N number of experiences
     */
    public GeometricBorwnianMotionMultipleCashDividends(double s0, double Y, double r, double sigma,
            double[] Dti, double[] ti, double T, int N) {
        this.Y = Y;
        this.sigma = sigma;
        this.T = T;
        this.N = N;
        if (ti.length != Dti.length) {
            throw new IllegalArgumentException("ti and Dti array lengths differ");
        }
        double sum = 0;
        for (int i = 0; i < ti.length; i++) {
            sum += Dti[i] * Math.exp(-r * ti[i]);
        }
        alpha = s0 - sum;
    }

    public double[] simulate(Random rng) {
        double[] answer = new double[N];
        for (int i = 0; i < N; i++) {
            answer[i] = alpha *
                Math.exp((Y - 0.5 * sigma * sigma) * T + sigma * Math.sqrt(T) * rng.nextGaussian());
        }
        return answer;
    }
}