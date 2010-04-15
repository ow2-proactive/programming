package org.objectweb.proactive.ext.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperty;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A quiet version of the stub generator
 *
 * When used from ant it is normal to have some warning about undeclared {@link PAProperty}.
 */
public class QuietStubGenerator {
    public static void main(String[] args) throws InterruptedException {
        Logger logger = ProActiveLogger.getLogger(Loggers.CONFIGURATION);
        logger.setLevel(Level.ERROR);
        StubGenerator.main(args);
    }
}
