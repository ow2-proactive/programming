package org.objectweb.proactive.extra.montecarlo;

import java.io.Serializable;

import java.util.Random;


/**
 * AbstractExperienceSetOutputFilter
 *
 * @author The ProActive Team
 */
public abstract class AbstractExperienceSetOutputFilter implements ExperienceSetOutputFilter, ExperienceSet {

    private ExperienceSet experienceSet;

    public AbstractExperienceSetOutputFilter(ExperienceSet experienceSet) {
        this.experienceSet = experienceSet;
    }

    public Serializable simulate(final Random rng) {
        Serializable results = experienceSet.simulate(rng);
        return filter(results);
    }

}
