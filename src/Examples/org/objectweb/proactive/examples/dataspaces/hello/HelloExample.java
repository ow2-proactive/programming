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
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.dataspaces.hello;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.exceptions.DataSpacesException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * Simple example of how to use Data Spaces in ProActive processing.
 * <p>
 * Goal of processing: count lines of documents from some sources (here as example - two Wikipedia
 * HTML pages accessed by HTTP) and store results in one file on the local disk.
 * <p>
 * Scenario:
 * <ol>
 * <li>GCM application is deployed with Data Spaces configured on deployed nodes and NamingService
 * started locally. According to GCMA Descriptor HTTP input resources are registered as named input
 * data spaces. Output file is registered as a default output data space.</li>
 * <li>Application-processing is delegated in {@link #exampleUsage()}. Two ActiveObjects start their
 * local processing in {@link ExampleProcessing#computePartials(String)} in parallel:
 * <ul>
 * <li>Read document content from specified named input data space</li>
 * <li>Count lines of a read document</li>
 * <li>Store partial result in a file within local scratch</li>
 * <li>Return URI of a file within local scratch containing partial results</li>
 * </ul>
 * </li>
 * <li>The deployer gathers partial URIs of files with partial results into a collection and calls
 * {@link ExampleProcessing#gatherPartials(Iterable)} method on one of the AOs, which aggregates
 * results. That method performs:
 * <ul>
 * <li>Read partial results from each specified scratch URI</li>
 * <li>Combine partial results as one list, a final results of the processing</li>
 * <li>Store final results in a file within the default output data space</li>
 * </ul>
 * </li>
 * <li>As GCM application stops.</li>
 * </ol>
 * <p>
 * Example is designed to work with provided GCMA descriptor that you can find in
 * examples/dataspaces/hello/helloApplication.xml. You can provide your GCMD descriptor (by setting
 * gcmd Java property to its path) or use example descriptors from the same directory.
 * <p>
 * To fulfill contract between the GCM descriptors and the application, following variable is set
 * from application:
 * <ul>
 * <li><code>{@link #VAR_OUTPUT_HOSTNAME}</code> name of a host that contains the output space is
 * being set to local hostname
 * </ul>
 * Application assumes that two named input spaces are available {@value #INPUT_RESOURCE1_NAME} and
 * {@value #INPUT_RESOURCE2_NAME} and default output for storing results. GCMD should provide at
 * least two nodes, each of it having scratch space defined. They are accessed from GCMA virtual
 * node {@value #VIRTUAL_NODE_NAME}.
 */
public class HelloExample {

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /**
     * Name of a host for output data space.
     */
    public static final String VAR_OUTPUT_HOSTNAME = "OUTPUT_HOSTNAME";

    public static final String VIRTUAL_NODE_NAME = "Hello";

    // @snippet-start DataSpacesExample_code_constants
    public static final String INPUT_RESOURCE1_NAME = "wiki_proactive";

    public static final String INPUT_RESOURCE2_NAME = "wiki_grid_computing";

    // @snippet-end DataSpacesExample_code_constants

    /**
     * @param args
     * @throws URISyntaxException
     * @throws ProActiveException
     * @throws NotConfiguredException
     * @throws IOException
     */
    public static void main(String[] args) throws NotConfiguredException, ProActiveException,
            URISyntaxException, IOException {
        if (args.length != 1) {
            System.out.println("Usage: java " + HelloExample.class.getName() +
                " <application descriptor filename>");
            System.exit(0);
        }

        new HelloExample().run(args[0]);
    }

    private List<Node> nodesDeployed;

    private GCMApplication gcmApplication;

    private VariableContractImpl vContract;

    /**
     * Starts NamingService, deploys application, executes example, and undeploys application and
     * stops NamingService.
     * 
     * @param descriptorPath
     *            path to deployment descriptor
     * @throws ProActiveException
     * @throws URISyntaxException
     */
    public void run(String descriptorPath) throws ProActiveException, URISyntaxException {
        setupVariables();

        try {
            startGCM(descriptorPath);
            exampleUsage();
        } catch (Exception x) {
            logger.error("Error: ", x);
        } finally {
            stop();
        }
    }

    // @snippet-start DataSpacesExample_code_variables
    private void setupVariables() {
        vContract = new VariableContractImpl();
        // this way of getting hostname is not the best solution, but it makes
        // local execution of example possible without using protocols like SFTP
        vContract.setVariableFromProgram(VAR_OUTPUT_HOSTNAME, Utils.getHostname(),
                VariableContractType.ProgramVariable);
    }

    // @snippet-end DataSpacesExample_code_variables

    private void stop() {
        stopGCM();
        logger.info("Application stopped");
        PALifeCycle.exitSuccess();
    }

    // @snippet-start DataSpacesExample_code_startgcm
    private void startGCM(String descriptorPath) throws ProActiveException {
        gcmApplication = PAGCMDeployment.loadApplicationDescriptor(new File(descriptorPath), vContract);
        gcmApplication.startDeployment();

        final GCMVirtualNode vnode = gcmApplication.getVirtualNode(VIRTUAL_NODE_NAME);
        vnode.waitReady();

        // grab nodes here
        nodesDeployed = vnode.getCurrentNodes();
        logger.info("Nodes started: " + nodesDeployed.size() + " nodes deployed");
    }

    // @snippet-end DataSpacesExample_code_startgcm

    private void stopGCM() {
        if (gcmApplication == null)
            return;
        gcmApplication.kill();
    }

    // real processing
    // @snippet-start DataSpacesExample_code_scenario
    private void exampleUsage() throws ActiveObjectCreationException, NodeException, DataSpacesException {
        checkEnoughRemoteNodesOrDie(2);
        final Node nodeA = nodesDeployed.get(0);
        final Node nodeB = nodesDeployed.get(1);

        final ExampleProcessing processingA = PAActiveObject.newActive(ExampleProcessing.class, null, nodeA);
        final ExampleProcessing processingB = PAActiveObject.newActive(ExampleProcessing.class, null, nodeB);
        final Collection<StringWrapper> partialResults = new ArrayList<StringWrapper>();
        try {
            partialResults.add(processingA.computePartials(INPUT_RESOURCE1_NAME));
            partialResults.add(processingB.computePartials(INPUT_RESOURCE2_NAME));
        } catch (IOException x) {
            logger.error("Could not store partial results", x);
            return;
        }

        try {
            processingB.gatherPartials(partialResults);
        } catch (IOException x) {
            logger.error("Could not write final results file", x);
        }
    }

    // @snippet-end DataSpacesExample_code_scenario

    private void checkEnoughRemoteNodesOrDie(int i) throws IllegalStateException {
        if (nodesDeployed.size() < i) {
            logger.error("Not enough nodes to run example");
            throw new IllegalStateException("Not enough nodes to run example");
        }
    }
}
