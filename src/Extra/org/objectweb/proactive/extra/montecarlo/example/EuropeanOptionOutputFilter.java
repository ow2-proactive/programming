package org.objectweb.proactive.extra.montecarlo.example;

import org.objectweb.proactive.extra.montecarlo.AbstractExperienceSetOutputFilter;
import org.objectweb.proactive.extra.montecarlo.basic.GeometricBrownianMotion;

import java.io.Serializable;


/**
 * EuropeanOptionOutputFilter
 *
 * @author The ProActive Team
 */
public class EuropeanOptionOutputFilter extends AbstractExperienceSetOutputFilter {
    private double strikePrice;

    public EuropeanOptionOutputFilter(GeometricBrownianMotion experienceSet, double strikePrice) {
        super(experienceSet);
        this.strikePrice = strikePrice;

    }

    public Serializable filter(Serializable experiencesResults) {

        double[] simulatedPrice = (double[]) experiencesResults;
        double payoffCall = 0, payoffPut = 0;
        for (int j = 0; j < simulatedPrice.length; j++) {
            payoffCall += Math.max(simulatedPrice[j] - this.strikePrice, 0);
            payoffPut += Math.max(this.strikePrice - simulatedPrice[j], 0);
        }
        return new PayOff(payoffCall, payoffPut);
    }

    public class PayOff implements Serializable {
        private double payoffCall;
        private double payoffPut;

        public PayOff(double payoffCall, double payoffPut) {
            this.payoffCall = payoffCall;
            this.payoffPut = payoffPut;
        }

        public double getPayoffCall() {
            return payoffCall;
        }

        public double getPayoffPut() {
            return payoffPut;
        }
    }
}
