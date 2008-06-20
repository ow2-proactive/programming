package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.exceptions.ReductionException;


/**
 * This interface declares the method for defining the reduction of result in multicast interfaces.
 *
 * @author The ProActive Team
 *
 */
@PublicAPI
public interface ReduceBehavior {

    /**
     * Reduces the list of values provided according to an algorithm in order to get the result wishes.
     *
     * @param the list of values which represents the list of the results of a call on a multicat interface.
     * @return the results reduced.
     * @throws ReductionException if reduction fails
     */
    public Object reduce(List<?> values) throws ReductionException;
}
