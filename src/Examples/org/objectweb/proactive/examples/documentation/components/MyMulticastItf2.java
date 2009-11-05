/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.documentation.components;

//@snippet-start component_userguide_7
import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.Reduce;
import org.objectweb.proactive.core.component.type.annotations.multicast.ReduceMode;
import org.objectweb.proactive.examples.documentation.classes.T;

import org.objectweb.proactive.examples.documentation.components.GetLastReduction;


public interface MyMulticastItf2 {

    //@snippet-break component_userguide_7
    //@snippet-start component_userguide_6
    public List<T> foo();

    public void bar();

    //@snippet-end component_userguide_6

    //@snippet-start component_userguide_8
    @Reduce(reductionMode = ReduceMode.CUSTOM, customReductionMode = GetLastReduction.class)
    public T foobar();

    //@snippet-end component_userguide_8

    //@snippet-resume component_userguide_7
    @Reduce(reductionMode = ReduceMode.SELECT_UNIQUE_VALUE)
    public T baz();
}
//@snippet-end component_userguide_7
