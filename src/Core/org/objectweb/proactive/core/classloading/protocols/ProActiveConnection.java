package org.objectweb.proactive.core.classloading.protocols;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class ProActiveConnection extends URLConnection {

    final static private Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);
    final private byte[] bytes;

    public ProActiveConnection(URL url) {
        this(url, null);
    }

    public ProActiveConnection(URL url, byte[] bytes) {
        super(url);
        this.bytes = bytes;
    }

    @Override
    public void connect() throws IOException {
        // DO NOTHING
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        } else {
            throw new IOException("This method must not be called when bytes is null");
        }
    }

}
