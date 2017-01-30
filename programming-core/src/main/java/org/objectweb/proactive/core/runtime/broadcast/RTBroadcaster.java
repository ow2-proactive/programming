/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.runtime.broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;


public class RTBroadcaster implements Runnable, RTBroadcasterAction, RTBroadcasterMessage {

    //
    // -- CONSTANT -----------------------------------------------
    //
    public static final String CREATION_HEADER = "creation:";

    public static final String DISCOVER_HEADER = "discover:";

    public static final int BUFFER_SIZE = 256;

    //
    // -- ATTRIBUTE -----------------------------------------------
    //
    private InetAddress address = null;

    private Vector<RTBroadcastAction> rtAction = new Vector<RTBroadcastAction>();

    private volatile boolean isAlive;

    private LocalBTCallback myBtCallback;

    private BTCallback remoteBtCallback;

    private URI uriMyBtCallback;

    private static boolean issetCallback;

    // --Singleton
    private static RTBroadcaster rtBroadcaster;

    //
    // -- CONSTRUCTOR -----------------------------------------------
    //
    private RTBroadcaster() throws BroadcastDisabledException {
        // --Address
        try {
            address = InetAddress.getByName(CentralPAPropertyRepository.PA_RUNTIME_BROADCAST_ADDRESS.getValueAsString());

            //--The property is specified

            @SuppressWarnings("unchecked")
            Class<? extends LocalBTCallback> btCallbackClass = (Class<? extends LocalBTCallback>) Class.forName(CentralPAPropertyRepository.PA_RUNTIME_BROADCAST_CALLBACK_CLASS.getValueAsString());

            myBtCallback = btCallbackClass.newInstance();

            isAlive = true;
            issetCallback = false;

            // create a remote object exposer for this object
            RemoteObjectExposer<BTCallback> roe = PARemoteObject.newRemoteObject(BTCallback.class.getName(),
                                                                                 (BTCallback) myBtCallback);

            String name = ProActiveRuntimeImpl.getProActiveRuntime().getURL() + "_rtBroadcast";

            // generate an uri where to rebind the runtime
            uriMyBtCallback = URI.create(name);

            remoteBtCallback = PARemoteObject.bind(roe, uriMyBtCallback);

        } catch (Exception e) {
            throw new BroadcastDisabledException("An exception occured when creating the RTBroadcaster. It is now disabled",
                                                 e);
        }
    }

    //
    // -- GETTER SINGLETON -----------------------------------------------
    //
    /**
     * Returns the instance of the RTBroadcaster associated to this runtime.
     *
     */
    public static synchronized RTBroadcaster getInstance() throws BroadcastDisabledException {

        if (!CentralPAPropertyRepository.PA_RUNTIME_BROADCAST.isTrue()) {
            throw new BroadcastDisabledException("broadcast discovery is disabled for this runtime");
        }

        if (rtBroadcaster == null) {
            rtBroadcaster = new RTBroadcaster();
            // wrap into a thread and start it
            new Thread(rtBroadcaster, "Thread for RTBroadcast").start(); // ref on the thread ??,
        }

        return rtBroadcaster;
    }

    public URI getCallbackUri() {
        return uriMyBtCallback;
    }

    public LocalBTCallback getLocalBTCallback() {
        return (LocalBTCallback) myBtCallback;
    }

    //
    // -- LISTENING THREAD -----------------------------------------------
    //
    public void run() {

        // --Initialize

        try {

            MulticastSocket socket = new MulticastSocket(CentralPAPropertyRepository.PA_RUNTIME_BROADCAST_PORT.getValue());
            socket.joinGroup(address); // BLOCKING ???

            DatagramPacket packet = null;

            while (getIsAlive()) // add a isAlive variable (kill() must be synchronized !!)
            {

                /***************************************************************
                 * Receive *
                 **************************************************************/

                byte[] buf = new byte[BUFFER_SIZE];
                packet = new DatagramPacket(buf, buf.length);

                // --Wait new packet
                socket.receive(packet);

                // --Display packet
                String received = new String(packet.getData(), 0, packet.getLength());

                // --creationHandler
                Pattern patternCreation = Pattern.compile(CREATION_HEADER + ".+");
                Matcher m = patternCreation.matcher(received);
                if (m.find()) {
                    received = received.replaceFirst(CREATION_HEADER, "");
                    // action.creationHandler(received);
                    for (Iterator iterator = rtAction.iterator(); iterator.hasNext();) {
                        RTBroadcastAction aRTBroadcastAction = (RTBroadcastAction) iterator.next();
                        aRTBroadcastAction.creationHandler(received);
                    }
                } else {
                    // --discoverHandler
                    Pattern patternDiscover = Pattern.compile(DISCOVER_HEADER + ".+");
                    m = patternDiscover.matcher(received);
                    if (m.find()) {
                        received = received.replaceFirst(DISCOVER_HEADER, "");
                        // action.discoverHandler(received);
                        for (Iterator iterator = rtAction.iterator(); iterator.hasNext();) {
                            RTBroadcastAction aRTBroadcastAction = (RTBroadcastAction) iterator.next();
                            aRTBroadcastAction.discoverHandler(received);
                        }
                    }
                }

            }

            // --Close connection
            socket.leaveGroup(address);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BroadcastDisabledException e) {
            e.printStackTrace();
        }

    }

    //
    // -- KILL THREAD -----------------------------------------------
    //
    public synchronized void kill() {
        isAlive = false;
    }

    //
    // -- ISALIVE GETTER -----------------------------------------------
    //
    public synchronized boolean getIsAlive() {
        return isAlive && (rtBroadcaster != null);
    }

    //
    // -- On signale notre presence a tout le monde
    // -----------------------------------------------
    //
    public void sendCreation() throws IOException {
        // declare the new runtime on the network
        sendToAll(CREATION_HEADER + uriMyBtCallback);
    }

    //
    // -- Which runtime are available ?
    // -----------------------------------------------
    //
    public void sendDiscover() throws IOException {
        // hello who's there ?
        sendToAll(DISCOVER_HEADER + uriMyBtCallback);
    }

    //
    // -- Broadcast a message -----------------------------------------------
    //
    private void sendToAll(String msg) throws IOException {

        // --Initialize
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = new byte[BUFFER_SIZE];
        buf = msg.getBytes();

        // send it
        InetAddress group = InetAddress.getByName(CentralPAPropertyRepository.PA_RUNTIME_BROADCAST_ADDRESS.getValueAsString());
        DatagramPacket packet = new DatagramPacket(buf,
                                                   buf.length,
                                                   group,
                                                   CentralPAPropertyRepository.PA_RUNTIME_BROADCAST_PORT.getValue());
        socket.send(packet);

    }

    //
    // -- Handling of RTBroadcastActions
    // -----------------------------------------------
    //
    public void addRTBroadcastAction(RTBroadcastAction action) {
        rtAction.add(action);
    }

    public void removeRTBroadcastAction(RTBroadcastAction action) {
        rtAction.remove(action);
    }

    /**
     * Warning : clone !
     * @return
     */
    @SuppressWarnings("unchecked")
    public Vector<RTBroadcastAction> listRTBroadcastAction() {
        return (Vector<RTBroadcastAction>) rtAction.clone();
    }
}
