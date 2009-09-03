package org.objectweb.proactive.extensions.vfsprovider.console;

import java.io.IOException;
import java.net.URISyntaxException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;


/**
 * Class for starting PA Provider server manually.
 */
public class PAProviderServerStarter {

    private static String rootDirectory;

    private static String providerName;

    private static FileSystemServerDeployer deployer;

    /**
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws IOException {
        final String name = PAProviderServerStarter.class.getName();

        try {
            parseArgs(args);
        } catch (IllegalArgumentException e) {
            System.out.println("Usage: java " + name + " <root directory> [PAProvider name]");
            System.out.println("       java " + name + " --help");
            System.out
                    .println("Starts the PA Provider server for <root directory> with default or specified name.");
            System.out.println("\t--help\tprints this screen");
            return;
        }

        setupHook();
        startServer();
    }

    private static void parseArgs(String[] args) {
        final int len = args.length;

        if (len == 0 || len > 2 || (len == 1 && "--help".equals(args[0])))
            throw new IllegalArgumentException();

        rootDirectory = args[0];
        if (len == 2)
            providerName = args[1];
    }

    private static void setupHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    stopServer();
                } catch (ProActiveException e) {
                    throw new ProActiveRuntimeException(e);
                }
            }
        });
    }

    private static void startServer() throws IOException {
        if (providerName == null)
            deployer = new FileSystemServerDeployer(rootDirectory, true);
        else
            deployer = new FileSystemServerDeployer(providerName, rootDirectory, true);

        final String url = deployer.getVFSRootURL();
        System.out.println("PAProvider successfully started.\nVFS URL of this provider: " + url);
    }

    private static void stopServer() throws ProActiveException {
        deployer.terminate();
    }
}
