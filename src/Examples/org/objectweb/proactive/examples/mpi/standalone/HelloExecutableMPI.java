package org.objectweb.proactive.examples.mpi.standalone;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PALifeCycle; //import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger; //import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


public class HelloExecutableMPI implements java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    public static void main(String[] args) throws Exception {

        GCMApplication applicationDescriptor = PAGCMDeployment.loadApplicationDescriptor(new File(args[0]));
        applicationDescriptor.startDeployment();

        Thread.sleep(10000); //workaround

        PALifeCycle.exitSuccess();
    }
}
