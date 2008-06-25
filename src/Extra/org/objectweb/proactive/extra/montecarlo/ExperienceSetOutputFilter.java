package org.objectweb.proactive.extra.montecarlo;

import java.io.Serializable;


/**
 * ExperienceSetOutputFilter
 *
 * @author The ProActive Team
 */
public interface ExperienceSetOutputFilter {

    Serializable filter(Serializable experiencesResults);
}
