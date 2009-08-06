package org.objectweb.proactive.examples.documentation.faulttolerance;

import java.io.IOException;

import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.process.JVMProcessImpl;


public class Main {
    public void launchServer() throws IOException {
        //@snippet-start fault_tolerance_1
        JVMProcessImpl jvmProcessImpl = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        jvmProcessImpl.setClassname("org.objectweb.proactive.core.body.ft.servers.StartFTServer");

        // optional line: Default arguments
        jvmProcessImpl.setParameters("-proto cic -name FTServer -port 1100 -fdperiod 30");

        jvmProcessImpl.startProcess();
        //@snippet-end fault_tolerance_1
    }

    public static void main(String[] args) {
        Main server = new Main();

        try {
            server.launchServer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
