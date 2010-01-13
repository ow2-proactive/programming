/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.component.collectiveitf.multicast;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;


public interface MulticastTestItf {
    // public void
    // processOutputMessage(@ParamDispatchMetadata(mode=ParamDispatchMode.ONE_TO_ONE)
    // List<Message> message);
    List<WrappedInteger> testBroadcast_Param(@ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST)
    List<WrappedInteger> list);

    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST))
    List<WrappedInteger> testBroadcast_Method(List<WrappedInteger> list);

    List<WrappedInteger> testOneToOne_Param(@ParamDispatchMetadata(mode = ParamDispatchMode.ONE_TO_ONE)
    List<WrappedInteger> list);

    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.ONE_TO_ONE))
    List<WrappedInteger> testOneToOne_Method(List<WrappedInteger> list);

    List<WrappedInteger> testRoundRobin_Param(@ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN)
    List<WrappedInteger> list);

    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN))
    List<WrappedInteger> testRoundRobin_Method(List<WrappedInteger> list);

    List<WrappedInteger> testCustom_Param(
            @ParamDispatchMetadata(mode = ParamDispatchMode.CUSTOM, customMode = CustomParametersDispatch.class)
            List<WrappedInteger> list);

    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.CUSTOM, customMode = CustomParametersDispatch.class))
    List<WrappedInteger> testCustom_Method(List<WrappedInteger> list);

    List<WrappedInteger> testAllStdModes_Param(List<WrappedInteger> list1,
            @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST)
            List<WrappedInteger> list2, @ParamDispatchMetadata(mode = ParamDispatchMode.ONE_TO_ONE)
            List<WrappedInteger> list3, @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN)
            List<WrappedInteger> list4, WrappedInteger a);
}
