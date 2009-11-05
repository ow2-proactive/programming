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

//@snippet-start component_userguide_2
//@snippet-start component_userguide_3
import java.util.List;
import org.objectweb.proactive.examples.documentation.classes.T;

//@snippet-break component_userguide_3
import org.objectweb.proactive.core.component.type.annotations.multicast.ClassDispatchMetadata;

//@snippet-resume component_userguide_3
//@snippet-break component_userguide_2
import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;

//@snippet-resume component_userguide_2
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;

//@snippet-break component_userguide_2
//@snippet-break component_userguide_3
import org.objectweb.proactive.core.group.Dispatch;
import org.objectweb.proactive.core.group.DispatchMode;


//@snippet-resume component_userguide_3
//@snippet-resume component_userguide_2

//@snippet-break component_userguide_3
@ClassDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST))
//@snippet-resume component_userguide_3
interface MyMulticastItf {

    //@snippet-break component_userguide_2
    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST))
    //@snippet-start component_userguide_4
    //@snippet-break component_userguide_3
    @Dispatch(mode = DispatchMode.DYNAMIC, bufferSize = 1024)
    //@snippet-resume component_userguide_2
    //@snippet-resume component_userguide_3
    public void foo(List<T> parameters);

    //@snippet-end component_userguide_4

    //@snippet-break component_userguide_2
    //@snippet-break component_userguide_3
    //@snippet-start component_userguide_5
    public void bar(@ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST)
    List<T> parameters);
    //@snippet-end component_userguide_5
    //@snippet-resume component_userguide_2
    //@snippet-resume component_userguide_3

}
//@snippet-end component_userguide_2
//@snippet-end component_userguide_3