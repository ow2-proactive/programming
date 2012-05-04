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
package org.objectweb.proactive.examples.documentation.filetransfer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PAFileTransfer;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.filetransfer.RemoteFile;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


//@snippet-start FileTransferTest
/**
 * @author ffonteno
 *
 * This class has been made to test the ProActive File Transfer and
 * extract code snippets for the documentation.
 *
 */
public class FileTransferTest {

    /**
     * Returns the virtual node whose name is VNName and described in the GCM descriptor file whose
     * path is descriptorPath
     *
     * @param descriptorPath path of the GCM descriptor file
     * @param VNName name of the virtual node
     * @return the virtual node whose name is VNName and described in the GCM descriptor file whose
     * path is descriptorPath
     */
    public static GCMVirtualNode getGCMVirtualNode(String descriptorPath, String VNName) {
        // Retrieves the file corresponding to your application descriptor
        File applicationDescriptor = new File(descriptorPath);

        GCMApplication gcmad;
        try {

            // Loads the application descriptor file
            gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);

            // Starts the deployment
            gcmad.startDeployment();

            GCMVirtualNode vn = gcmad.getVirtualNode(VNName);
            vn.waitReady();

            return vn;

        } catch (ProActiveException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Copies the file whose path is sourcePath on the local host to the file with path destPath
     * on each machine that hosts a node mapped with the virtual node VNName
     *
     * @param descriptorPath path of the GCM descriptor file
     * @param VNName name of the virtual node
     * @param sourcePath source path
     * @param destPath destination path
     * @throws IOException
     * @throws NodeException
     */
    public static void testGCMFileTransfer(String descriptorPath, String VNName, String sourcePath,
            String destPath) throws IOException, NodeException {

        Node srcNode = NodeFactory.getDefaultNode();
        System.out.println(srcNode.getVMInformation().getHostName());

        GCMVirtualNode vn = FileTransferTest.getGCMVirtualNode(descriptorPath, VNName);
        long nbNodes = vn.getNbCurrentNodes();

        for (long l = 0; l < nbNodes; l++) {
            System.out.println("Node number " + l);
            Node destNode = vn.getANode();
            System.out.println(destNode.getVMInformation().getHostName());
            RemoteFile rf = PAFileTransfer.transfer(srcNode, new File(sourcePath), destNode, new File(
                destPath));
            rf.waitFor();
            System.out.println(rf.getRemoteFilePath().getPath());
        }
    }

    /**
     * Returns the virtual node whose name is VNName and described in the XML descriptor file whose
     * path is descriptorPath
     *
     * @param descriptorPath path of the XML descriptor file
     * @param VNName name of the virtual node
     * @return the virtual node whose name is VNName and described in the GCM descriptor file whose
     * path is descriptorPath
     * @throws ProActiveException
     */
    public static VirtualNode getXMLVirtualNode(String descriptorPath, String VNName)
            throws ProActiveException {

        // Creates the ProActiveDescriptor corresponding to the descriptor file
        ProActiveDescriptor proActiveDescriptor = PADeployment.getProactiveDescriptor(descriptorPath);

        // Gets the virtual node named VN1 described in the descriptor file.
        VirtualNode virtualNode = proActiveDescriptor.getVirtualNode(VNName);

        // Activates the virtual node.
        // For activating several virtual node at once, you can use
        // proActiveDescriptorAgent.activateMappings()
        virtualNode.activate();

        return virtualNode;
    }

    /**
     * Copies the file whose path is sourcePath on the local host to the file with path destPath
     * on each machine that hosts a node mapped with the virtual node VNName
     *
     * @param descriptorPath path of the XML descriptor file
     * @param VNName name of the virtual node
     * @param sourcePath source path
     * @param destPath destination path
     * @throws IOException
     * @throws ProActiveException
     */
    public static void testXMLFileTransfer(String descriptorPath, String VNName, String sourcePath,
            String destPath) throws IOException, ProActiveException {

        Node srcNode = NodeFactory.getDefaultNode();
        System.out.println(srcNode.getVMInformation().getHostName());

        VirtualNode virtualNode = FileTransferTest.getXMLVirtualNode(descriptorPath, VNName);

        long nbNodes = virtualNode.getNbMappedNodes();

        for (long l = 0; l < nbNodes; l++) {
            System.out.println("Node number " + l);
            Node destNode = virtualNode.getNode();
            System.out.println(destNode.getVMInformation().getHostName());
            RemoteFile rf = PAFileTransfer.transfer(srcNode, new File(sourcePath), destNode, new File(
                destPath));
            rf.waitFor();
            System.out.println(rf.getRemoteFilePath().getPath());
        }

        //@snippet-break FileTransferTest
        try {
            // @snippet-start FileTransfer_4
            List<RemoteFile> rfList = virtualNode.getVirtualNodeInternal().fileTransferRetrieve();
            // @snippet-end FileTransfer_4
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //@snippet-resume FileTransferTest
    }

    /**
     * Test ProActive File Transfer with the two deployments.
     *
     * @param args should be: (GCMA.xml|Descriptor.xml) VirtualNodeName sourcePath destPath
     * @throws ProActiveException
     */
    public static void main(String[] args) throws ProActiveException {
        try {
            if (args.length < 4) {
                System.out.println("Wrong number of arguments");
                System.out
                        .println("Usage: java FileTransferTest (GCMA.xml|Descriptor.xml) VirtualNodeName sourcePath destPath");
            }
            //testGCMFileTransfer(args[0], args[1], args[2], args[3]);
            testXMLFileTransfer(args[0], args[1], args[2], args[3]);
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
//@snippet-end FileTransferTest