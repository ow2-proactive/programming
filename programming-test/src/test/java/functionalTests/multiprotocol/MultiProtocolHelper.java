/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionalTests.multiprotocol;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


/**
 * MultiProtocolHelper
 *
 * @author The ProActive Team
 **/
public class MultiProtocolHelper {

    /**
     * Deploys a separate JVM with the given set of protocols, the first element of the set
     * will be the default protocol, and other ones will be additional protocols
     * @param proto list of protocols
     * @param gcma url of the application descriptor
     * @param variableContract
     * @param jvmParameters
     * @return aNode
     * @throws CloneNotSupportedException
     */
    public static Node deployANodeWithProtocols(List<String> proto, URL gcma, VariableContractImpl variableContract,
            List<String> jvmParameters) throws URISyntaxException, ProActiveException {

        for (Iterator<String> it = jvmParameters.iterator(); it.hasNext();) {
            String param = it.next();
            if (param.contains(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getName()) ||
                param.contains(PAMRConfig.PA_NET_ROUTER_ADDRESS.getName()) ||
                param.contains(PAMRConfig.PA_NET_ROUTER_PORT.getName())) {
                it.remove();
            }
        }
        // we add the router config properties in the jvm parameter
        if (PAMRConfig.PA_NET_ROUTER_ADDRESS.isSet()) {
            jvmParameters.add(PAMRConfig.PA_NET_ROUTER_ADDRESS.getCmdLine() +
                              PAMRConfig.PA_NET_ROUTER_ADDRESS.getValue());
        }
        if (PAMRConfig.PA_NET_ROUTER_PORT.isSet()) {
            jvmParameters.add(PAMRConfig.PA_NET_ROUTER_PORT.getCmdLine() + PAMRConfig.PA_NET_ROUTER_PORT.getValue());
        }

        // we update the variable contract with the new value of the jvm params
        StringBuilder sb = new StringBuilder();
        for (String param : jvmParameters) {
            sb.append(param).append(" ");
        }

        String finalJVMparams = sb.toString().trim();
        variableContract.setVariableFromProgram("JVM_PARAMETERS", finalJVMparams, VariableContractType.ProgramVariable);

        // we add in the variable contract the protocols
        variableContract.setVariableFromProgram("communication.protocol",
                                                proto.get(0),
                                                VariableContractType.ProgramVariable);

        String apstring = proto.get(1);
        for (int i = 2; i < proto.size(); i++) {
            apstring += "," + proto.get(i);
        }
        variableContract.setVariableFromProgram("additional.protocols", apstring, VariableContractType.ProgramVariable);

        GCMApplication gcmad = PAGCMDeployment.loadApplicationDescriptor(new File(gcma.toURI()), variableContract);
        gcmad.startDeployment();

        Node node = gcmad.getVirtualNode("VN").getANode();
        return node;
    }

    /**
     * This method computes a list of permutation from the input list of protocols
     *
     * @param l
     * @param fixed
     */
    public static void permute(HashSet<List<String>> permutations, ArrayList<String> l, int fixed) {
        if (fixed >= l.size() - 1) {
            // nothing left to permute --
            permutations.add((List<String>) l.clone());
        } else {
            // put each element from fixed+1...EOL in location fixed in turn

            // special case (no swaps) for leaving item in loc fixed as is
            permute(permutations, l, fixed + 1);

            // now try all the other elements in turn
            String x = l.get(fixed);
            for (int i = fixed + 1; i < l.size(); i++) {
                // swap elements at locations fixed and i
                l.set(fixed, l.get(i));
                l.set(i, x);

                // find all permutations of the elements after fixed
                permute(permutations, l, fixed + 1);

                // put things back the way they were
                l.set(i, l.get(fixed));
                l.set(fixed, x);
            }
        }
    }
}
