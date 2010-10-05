package org.objectweb.proactive.extensions.processbuilder;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.OperatingSystem;


/**
 * An {@link OSProcessBuilderFactory} integrated with ProActive. 
 */
public class PAOSProcessBuilderFactory implements OSProcessBuilderFactory {
    final private OperatingSystem os;
    final private String paHome;

    public PAOSProcessBuilderFactory() throws ProActiveException {
        this.os = OperatingSystem.getOperatingSystem();
        this.paHome = ProActiveRuntimeImpl.getProActiveRuntime().getProActiveHome();
    }

    public OSProcessBuilder getBuilder() {
        switch (os) {
            case unix:
                return new LinuxProcessBuilder(paHome);
            case windows:
                return new WindowsProcessBuilder(paHome);
            default:
                return new BasicProcessBuilder();
        }
    }
}
