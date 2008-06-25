package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.ExperienceSet;

import java.util.Random;


/**
 * GeometricBorwnianMotionCashDividend : Simulating geometric Brownian motion with a cash dividend before T
 *
 * @author The ProActive Team
 */
public class GeometricBorwnianMotionCashDividend implements ExperienceSet {

    private double s0, Y, r, sigma, Dt, t, T;
    private int N;

    /**
     * Simulating geometric Brownian motion with a cash dividend before T
     *
     * @param s0 Initial value at t=0 of geometric Brownian
     * @param Y Yield of the underlying, for stocks Y=r (interest rate), futures Y=0, currencies Y=(domestic interest rate-foreign interest rate)
     * @param r Continuous compounded interest rate
     * @param sigma Volatility
     * @param Dt Cash dividend amount at time t
     * @param t time of the cash dividend
     * @param T final time
     * @param N number of experiences
     */
    public GeometricBorwnianMotionCashDividend(double s0, double Y, double r, double sigma, double Dt,
            double t, double T, int N) {
        this.s0 = s0;
        this.Y = Y;
        this.r = r;
        this.sigma = sigma;
        this.Dt = Dt;
        this.t = t;
        this.T = T;
        this.N = N;
    }

    public double[] simulate(Random rng) {
        double[] answer = new double[N];
        for (int i = 0; i < N; i++) {
            answer[i] = (s0 - Dt * Math.exp(-r * t)) *
                Math.exp((Y - 0.5 * sigma * sigma) * T + sigma * Math.sqrt(T) * rng.nextGaussian());
        }
        return answer;
    }
}
