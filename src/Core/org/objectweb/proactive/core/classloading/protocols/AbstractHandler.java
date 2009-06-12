package org.objectweb.proactive.core.classloading.protocols;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class AbstractHandler extends URLStreamHandler {
    final static private Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        // WARNING: This method will break if URL mapping change
        //
        // URL must follow this convention:
        //   paproto://host[:port]/RUNTIME/pkg/classname.class
        //

        final String runtimeUrl;
        final String classname;
        final String uStr = u.toString();

        int index = 0;
        int count = 0;
        // Find the 4th / (aka the / just after RUNTIME)
        while (count != 4 && index < uStr.length()) {
            if (uStr.charAt(index) == '/') {
                count++;
            }

            index++;
        }

        if (count == 4) {
            // It is safe to skip the first to entry since the protocol handler
            // mechanism guarantee that the protocol is correct
            runtimeUrl = uStr.substring(2, index - 1);
        } else {
            throw new IOException("Unsupported URL: " + u);
        }

        if (index == uStr.length()) {
            classname = null;
        } else {
            if ((uStr.length() - index) <= ".class".length()) {
                throw new IOException("Unsupported URL: " + u);
            } else {
                classname = uStr.substring(index, uStr.length() - ".class".length()).replace('/', '.');
            }

        }

        if (classname != null) {
            try {
                ProActiveRuntime rt = RuntimeFactory.getRuntime(runtimeUrl);
                byte[] b = rt.getClassData(classname);

                if (b != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Succeffully downloaded " + classname + " from " + runtimeUrl);
                    }
                    return new ProActiveConnection(u, b);
                } else {
                    logger.info("Failed to download " + classname + " from " + runtimeUrl);
                    throw new IOException("Failed to download " + classname + " from " + runtimeUrl);
                }
            } catch (ProActiveException e) {
                logger.info("Failed to download " + classname + " from " + runtimeUrl);
                throw new IOException("Failed to download " + classname + " from " + runtimeUrl);
            }
        } else {
            return new ProActiveConnection(u);
        }

    }
}
