/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.runtime.broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
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
    private RTBroadcaster() {
        // --Address
        try {
            address = InetAddress.getByName(CentralPAPropertyRepository.PA_RUNTIME_BROADCAST_ADDRESS
                    .getValueAsString());
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        //--The property is specified
        try {
            Class<? extends LocalBTCallback> btCallbackClass;
            btCallbackClass = (Class<? extends LocalBTCallback>) Class
                    .forName(CentralPAPropertyRepository.PA_RUNTIME_BROADCAST_CALLBACK_CLASS
                            .getValueAsString());

            myBtCallback = btCallbackClass.newInstance();

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        isAlive = true;
        issetCallback = false;

        // create a remote object exposer for this object
        RemoteObjectExposer<BTCallback> roe = PARemoteObject.newRemoteObject(BTCallback.class.getName(),
                (BTCallback) myBtCallback);

        String name = ProActiveRuntimeImpl.getProActiveRuntime().getURL() + "_rtBroadcast";

        // generate an uri where to rebind the runtime
        uriMyBtCallback = URI.create(name);

        try {
            remoteBtCallback = PARemoteObject.bind(roe, uriMyBtCallback);
        } catch (UnknownProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //
    // -- GETTER SINGLETON -----------------------------------------------
    //

    /**
     * Returns the instance of {@link RTBroadcaster} is enabled or null is disabled.
     */
    public static synchronized RTBroadcaster getInstance() {
        if (CentralPAPropertyRepository.PA_RUNTIME_BROADCAST.getValue() == false) {
            return null;
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

            MulticastSocket socket = new MulticastSocket(
                CentralPAPropertyRepository.PA_RUNTIME_BROADCAST_PORT.getValue());
            socket.joinGroup(address); // BLOCKING ???

            DatagramPacket packet = null;

            while (getIsAlive()) // add a isAlive variable (kill() must be synchronized !!)
            {

                try {
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

                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }

            }

            // --Close connection
            socket.leaveGroup(address);
            socket.close();
        } catch (IOException e) {
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
    // -- On signale notre pr�sence � tout le monde
    // -----------------------------------------------
    //
    public void sendCreation() {
        // declare the new runtime on the network
        sendToAll(CREATION_HEADER + uriMyBtCallback);
    }

    //
    // -- Which runtime are available ?
    // -----------------------------------------------
    //
    public void sendDiscover() {
        // hello who's there ?
        sendToAll(DISCOVER_HEADER + uriMyBtCallback);
    }

    //
    // -- Broadcast a message -----------------------------------------------
    //
    private void sendToAll(String msg) {
        try {
            // --Initialize
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = new byte[BUFFER_SIZE];
            buf = msg.getBytes();

            // send it
            InetAddress group = InetAddress
                    .getByName(CentralPAPropertyRepository.PA_RUNTIME_BROADCAST_ADDRESS.getValueAsString());
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group,
                CentralPAPropertyRepository.PA_RUNTIME_BROADCAST_PORT.getValue());
            socket.send(packet);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
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
    public Vector<RTBroadcastAction> listRTBroadcastAction() {
        return (Vector<RTBroadcastAction>) rtAction.clone();
    }
}
