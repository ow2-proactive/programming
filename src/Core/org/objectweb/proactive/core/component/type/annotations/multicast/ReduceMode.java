package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.exceptions.ReductionException;


/**
 * <p>This enumeration defines the various reduction modes available for
 * methods. 
 * </p>
 * <p>It implements the method of the <code>ReduceBehavior</code> interface
 * depending on the selected mode.
 *
 * @author The ProActive Team
 * 
 */
@PublicAPI
public enum ReduceMode implements ReduceBehavior, Serializable {
    /**
     * This reduction mode allows to extract of the list of results the only one result that the list contains.
     */
    SELECT_UNIQUE_VALUE,

    /**
     * The reduction mode is given as a
     * parameter, as a class signature.
     */
    CUSTOM;

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.ReduceBehavior#reduce(java.util.List<?>)
     */
    public Object reduce(List<?> values) throws ReductionException {
        switch (this) {
            case SELECT_UNIQUE_VALUE:
                if (!(values.size() == 1)) {
                    throw new ReductionException(
                        "invalid number of values to reduce: expected [1] but received [" + values.size() +
                            "]");
                }
                return values.iterator().next();

            default:
                return SELECT_UNIQUE_VALUE.reduce(values);
        }
    }

}
