package org.objectweb.proactive.extra.messagerouting.router;

import java.io.IOException;
import java.net.InetAddress;

import org.objectweb.proactive.annotation.PublicAPI;


/** A ProActive message router 
 * 
 *  
 * A router receives messages from client and forward them to another client.
 * 
 * @since ProActive 4.1.0
 */
@PublicAPI
public abstract class Router {

    static public Router createAndStart(RouterConfig config) throws IOException {
        // config is now immutable
        config.setReadOnly();

        RouterImpl r = new RouterImpl(config);

        Thread rThread = new Thread(r);
        rThread.setName("Router: select");
        if (config.isDaemon()) {
            rThread.setDaemon(config.isDaemon());
        }
        rThread.start();

        return r;
    }

    /** Returns the port on which the router is, or was, listening */
    abstract public int getPort();

    /** Returns the {@link InetAddress} on which the router is, or was, listening */
    abstract public InetAddress getInetAddr();

    /** Stops the router
     * 
     * Terminates all the threads and unbind all the sockets.
     */
    abstract public void stop();
}
