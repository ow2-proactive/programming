/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.remoteobject.http.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.remoteobject.http.HTTPTransportServlet;
import org.objectweb.proactive.core.remoteobject.http.util.exceptions.HTTPRemoteException;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpMessageSender {
    final static private Logger logger = ProActiveLogger.getLogger(Loggers.HTTP_CLIENT);

    public static final String SERVICE_REQUEST_CONTENT_TYPE = "application/java";
    private String url;

    /**
     *
     * @param url
     */
    public HttpMessageSender(String url) {
        this.url = url;
    }

    /**
     *
     * @param message
     */
    public Object sendMessage(HttpMessage message) throws HTTPRemoteException {
        byte[] bytes = HttpMarshaller.marshallObject(message);

        String url_;

        //        try {
        //            url_ = UrlBuilder.checkUrl(url);
        //            System.out.println("************* " + url_);
        //        } catch (UnknownHostException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        }
        try {
            String nodename = null;
            if (!url.startsWith(Constants.XMLHTTP_PROTOCOL_IDENTIFIER)) {
                url = Constants.XMLHTTP_PROTOCOL_IDENTIFIER + url;
            }
            int lastslash = url.lastIndexOf('/');
            if (lastslash > 6) {
                URI u;
                u = new URI(url);
                nodename = u.getPath();
                url = URIBuilder.getProtocol(u) + "://" + u.getHost() + ":" + u.getPort();
            }
            int lastIndex = url.lastIndexOf(":");

            URL u = new URL(url + HTTPTransportServlet.NS);

            //connection to the specified url
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Length", "" + bytes.length);
            connection.setRequestProperty("Content-Type", SERVICE_REQUEST_CONTENT_TYPE);
            connection.setUseCaches(false);
            connection.connect();

            //write data in the stream
            BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());

            out.write(bytes);
            out.flush();
            out.close();

            //Get data returned in the connection
            DataInputStream in = null;
            in = new DataInputStream(new BufferedInputStream(connection.getInputStream()));

            byte[] buf = new byte[0];
            byte[] acc = new byte[1024];
            int acc_i = 0;
            boolean cont = true;
            while (cont) {
                try {
                    if (acc_i == acc.length) {
                        // acc is full flush it into buf
                        byte[] old = buf;
                        buf = new byte[old.length + acc.length];
                        System.arraycopy(old, 0, buf, 0, old.length);
                        System.arraycopy(acc, 0, buf, old.length, acc.length);

                        acc_i = 0;
                    }

                    acc[acc_i] = in.readByte();
                    acc_i++;
                } catch (EOFException e) {
                    cont = false;
                }
            }

            // flush acc into buf
            byte[] old = buf;
            buf = new byte[old.length + acc_i];
            System.arraycopy(old, 0, buf, 0, old.length);
            System.arraycopy(acc, 0, buf, old.length, acc_i);

            Object returnedObject = HttpMarshaller.unmarshallObject(buf);
            return returnedObject;

            //            if (returnedObject instanceof Exception)
            //                throw (Exception)returnedObject;
        } catch (ConnectException e) {
            throw new HTTPRemoteException("Error while connecting the remote host: " + url, e);
        } catch (UnknownHostException e) {
            throw new HTTPRemoteException("Unknown remote host: " + url, e);
        } catch (IOException e) {
            throw new HTTPRemoteException("Error during connection with remote host" + url, e);
        } catch (URISyntaxException e) {
            throw new HTTPRemoteException("Bad URL " + url, e);
        }
    }
}
