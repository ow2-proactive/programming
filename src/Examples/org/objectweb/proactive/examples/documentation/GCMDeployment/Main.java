package org.objectweb.proactive.examples.documentation.GCMDeployment;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

//@snippet-start VariableContract_import
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;

//@snippet-end VariableContract_import
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class Main {

    private final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    public static void defineVariableContract(String gcmaFile) {
        //@snippet-start VariableContract_2
        VariableContractImpl variableContract = new VariableContractImpl();
        variableContract.setVariableFromProgram("VIRTUAL_NODE_NAME", "testnode",
                VariableContractType.ProgramVariable);
        variableContract.setVariableFromProgram("NUMBER_OF_VIRTUAL_NODES", "10",
                VariableContractType.DescriptorDefaultVariable);
        variableContract.setVariableFromProgram("priority.queue", "vip",
                VariableContractType.JavaPropertyProgramDefault);

        File descriptor = new File(gcmaFile);
        GCMApplication gcma;
        try {
            gcma = PAGCMDeployment.loadApplicationDescriptor(descriptor, variableContract);

            //Usage example
            VariableContract vc = gcma.getVariableContract();
            String proActiveHome = vc.getValue("PROACTIVE_HOME");

        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //@snippet-end VariableContract_2
    }

    public static void deployByApplication(String descriptorPath) {
        //@snippet-start start_deployment
        // Retrieves the file corresponding to your application descriptor
        File applicationDescriptor = new File(descriptorPath);

        GCMApplication gcmad;
        try {

            //@snippet-start nodes_fixed_by_application
            // Loads the application descriptor file
            gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);

            // Starts the deployment
            gcmad.startDeployment();
            //@snippet-break start_deployment

            GCMVirtualNode vn = gcmad.getVirtualNode("Agent");

            List<Node> nodeList = vn.getCurrentNodes();
            while (nodeList.size() < 3) {

                //@snippet-break nodes_fixed_by_application
                logger.info("node list is composed of " + nodeList.size() + " nodes");
                logger.info("Too few nodes ...");
                //@snippet-resume nodes_fixed_by_application
                Thread.sleep(2000);
                nodeList = vn.getCurrentNodes();
            }
            //@snippet-end nodes_fixed_by_application
            logger.info("there are " + nodeList.size());
            //@snippet-resume start_deployment

            // ...

            // Terminates the deployment
            gcmad.kill();

        } catch (ProActiveException e) {
            e.printStackTrace();
        }
        //@snippet-end start_deployment
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void deployByDeployer(String descriptorPath) {
        // Retrieves the file corresponding to your application descriptor
        File applicationDescriptor = new File(descriptorPath);

        GCMApplication gcmad;
        try {

            //@snippet-start nodes_fixed_by_deployer
            // Loads the application descriptor file
            gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);

            // Starts the deployment
            gcmad.startDeployment();

            GCMVirtualNode vn = gcmad.getVirtualNode("Agent");
            vn.waitReady();
            //@snippet-end nodes_fixed_by_deployer

            logger.info(vn.getName() + " has " + vn.getNbCurrentNodes() + " nodes.");
            // Terminates the deployment
            gcmad.kill();

        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    public class OnDemand {

        public void nodeAttached(Node node, String vnName) {
            logger.info(vnName);
            logger.info(node.getNodeInformation().getName() + " has been attached !");
        }

        public void deployOnDemand(String descriptorPath) {
            // Retrieves the file corresponding to your application descriptor
            File applicationDescriptor = new File(descriptorPath);

            GCMApplication gcmad;
            try {

                //@snippet-start nodes_on_demand
                // Loads the application descriptor file
                gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);

                // Starts the deployment
                gcmad.startDeployment();

                GCMVirtualNode vn = gcmad.getVirtualNode("Agent");
                vn.subscribeNodeAttachment(this, "nodeAttached", false);

                // Waiting for new nodes
                Thread.sleep(5000);
                //@snippet-end nodes_on_demand

                // Terminates the deployment
                gcmad.kill();

            } catch (ProActiveException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        logger.info("Loading the following application descriptor: " + args[0]);
        deployByApplication(args[0]);
        //        deployByDeployer(args[0]);
        //        Main myMain = new Main();
        //        OnDemand onDemand = myMain.new OnDemand();
        //        onDemand.deployOnDemand(args[0]);
        //        System.exit(0);
    }

}
