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
package functionalTests.component.conform.components;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public interface SlaveMulticast {
    public void computeOneWay(@ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN)
    List<String> args, String other);

    public List<StringWrapper> computeAsync(@ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN)
    List<String> args, String other);

    public List<StringWrapper> computeRoundRobinBroadcastAsync(
            @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN)
            List<String> args, @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST)
            List<String> other);

    public List<GenericTypeWrapper<String>> computeAsyncGenerics(
            @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN)
            List<String> args, String other);

    public List<String> computeSync(@ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN)
    List<String> args, String other);

}
