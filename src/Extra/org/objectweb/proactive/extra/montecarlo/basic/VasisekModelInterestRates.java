package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.ExperienceSet;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.RandomStream;


/**
 * VasisekModelInterestRates
 *
 * This equation is the exact solution of the one-factor Vasicek interet rate model. In this model interest rates are Normal distributed, and thus can become negative.
 *
 * @author The ProActive Team
 */
public class VasisekModelInterestRates implements ExperienceSet<double[]> {

    private double a, b, t;
    private int N;

    /**
     * This equation is the exact solution of the one-factor Vasicek interet rate model. In this model interest rates are Normal distributed, and thus can become negative.
     * @param r0 Initial value at t=0 of the interest rate
     * @param alpha mean reversion rate
     * @param beta mean reversion level
     * @param sigma Volatility
     * @param t maturity date
     * @param N number of experiences
     */
    public VasisekModelInterestRates(double r0, double alpha, double beta, double sigma, double t, int N) {
        this.a = r0 * Math.exp(-alpha * t) + (beta / alpha) * (1 - Math.exp(-alpha * t));
        this.b = sigma * Math.sqrt((1 - Math.exp(-2 * alpha * t)) / (2 * alpha));
        this.t = t;
        this.N = N;

    }

    public double[] simulate(RandomStream rng) {
        double[] answer = new double[N];
        NormalGen ngen = new NormalGen(rng, new NormalDist());
        for (int i = 0; i < N; i++) {
            answer[i] = a + b * ngen.nextDouble();
        }
        return answer;
    }
}
