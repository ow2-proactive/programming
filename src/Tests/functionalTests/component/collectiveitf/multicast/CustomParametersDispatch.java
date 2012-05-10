/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.component.collectiveitf.multicast;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatch;


/**
 * A dummy distribution that only dispatches the first element of lists of n elements
 * 
 * @author The ProActive Team
 *
 */
public class CustomParametersDispatch implements ParamDispatch {

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#dispatch(java.lang.Object,
     *      int)
     */
    public List<Object> partition(Object inputParameter, int nbOutputReceivers)
            throws ParameterDispatchException {
        if (!(inputParameter instanceof List<?>) || !(((List<?>) inputParameter).size() >= 1) ||
            !(((List<?>) inputParameter).get(0) instanceof WrappedInteger)) {
            throw new ParameterDispatchException("needs a List of (at least 1) WrappedInteger elements");
        }

        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < nbOutputReceivers; i++) {
            result.add(((List<?>) inputParameter).get(0));
        }
        return result;
    }

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#expectedDispatchSize(java.lang.Object,
     *      int)
     */
    public int expectedDispatchSize(Object inputParameter, int nbOutputReceivers)
            throws ParameterDispatchException {
        return nbOutputReceivers;
    }

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#match(java.lang.reflect.Type,
     *      java.lang.reflect.Type)
     */
    public boolean match(Type clientSideInputParameterType, Type serverSideInputParameterType)
            throws ParameterDispatchException {
        try {
            boolean one = ((Class<?>) ((ParameterizedType) clientSideInputParameterType).getRawType())
                    .equals(List.class);
            boolean two = ((Class<?>) ((ParameterizedType) clientSideInputParameterType)
                    .getActualTypeArguments()[0]).equals(WrappedInteger.class);
            boolean three = ((Class<?>) serverSideInputParameterType).equals(WrappedInteger.class);
            return one && two && three;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
