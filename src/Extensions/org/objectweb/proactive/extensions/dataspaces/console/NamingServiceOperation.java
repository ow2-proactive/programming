package org.objectweb.proactive.extensions.dataspaces.console;

import java.net.URISyntaxException;

import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.WrongApplicationIdException;


public class NamingServiceOperation {

    /**
     * @param args
     */
    public static void main(String[] args) throws ProActiveException, URISyntaxException {
        if (args.length != 4) {
            leave();
            return;
        }

        final long applicatiodID;
        final String url = args[0];
        final String appIdString = args[1];
        final String inputName = args[2];
        final String inputURL = args[3];

        try {
            applicatiodID = Long.parseLong(appIdString);
        } catch (NumberFormatException e) {
            leave(e.getMessage());
            return;
        }

        try {
            final InputOutputSpaceConfiguration conf = InputOutputSpaceConfiguration
                    .createInputSpaceConfiguration(inputURL, null, null, inputName);
            final SpaceInstanceInfo spaceInstanceInfo = new SpaceInstanceInfo(applicatiodID, conf);
            NamingService stub = NamingService.createNamingServiceStub(url);

            try {
                stub.register(spaceInstanceInfo);
            } catch (WrongApplicationIdException e) {
                stub.registerApplication(applicatiodID, null);
                stub.register(spaceInstanceInfo);
            }
        } finally {
            PALifeCycle.exitSuccess();
        }
    }

    private static void leave(String message) {
        if (message != null)
            System.out.println("Error: " + message);
        leave();
    }

    private static void leave() {
        final String name = NamingServiceOperation.class.getName();

        System.out.println("Usage: java " + name +
            " <naming service URL> <application id> <input name> <input URL>");
        System.out.println("Registers input with specified name and URL.");
        System.out.println("\t--help\tprints this screen");
    }

}
